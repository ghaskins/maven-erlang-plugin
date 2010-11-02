package eu.lindenbaum.maven.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ericsson.otp.erlang.OtpAuthException;
import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangExit;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpPeer;
import com.ericsson.otp.erlang.OtpSelf;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Containing utilities related to erlang code execution.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class ErlUtils {
  /**
   * Executes the given erlang script on a remote erlang node using RPC.
   * 
   * @param expression to evaluate
   * @param connection to use for RPC communication
   * @return the term representing the script result
   * @throws MojoExecutionException
   */
  public static OtpErlangObject eval(String expression, OtpConnection connection) throws MojoExecutionException {
    try {
      connection.sendRPC("erl_eval", "new_bindings", new OtpErlangList());
      OtpErlangObject bindings = connection.receiveRPC();

      connection.sendRPC("erl_scan", "string", new OtpErlangList(new OtpErlangString(expression)));
      OtpErlangTuple result = (OtpErlangTuple) connection.receiveRPC();
      OtpErlangObject indicator = result.elementAt(0);
      if ("ok".equals(indicator.toString())) {
        connection.sendRPC("erl_parse", "parse_exprs", new OtpErlangList(result.elementAt(1)));
        result = (OtpErlangTuple) connection.receiveRPC();
        indicator = result.elementAt(0);
        if ("ok".equals(indicator.toString())) {
          OtpErlangList forms = (OtpErlangList) result.elementAt(1);
          if (forms.arity() > 0) {
            connection.sendRPC("erl_eval", "expr", new OtpErlangObject[]{ forms.getHead(), bindings });
            result = (OtpErlangTuple) connection.receiveRPC();
            indicator = result.elementAt(0);
            if ("value".equals(indicator.toString())) {
              return result.elementAt(1);
            }
            else {
              OtpErlangObject errorInfo = result.elementAt(1);
              throw new MojoExecutionException("failed to evaluate form: " + errorInfo.toString());
            }
          }
          else {
            throw new MojoExecutionException("couldn't find forms to evaluate in expression");
          }
        }
        else {
          OtpErlangObject errorInfo = result.elementAt(1);
          throw new MojoExecutionException("failed to parse tokens: " + errorInfo.toString());
        }
      }
      else {
        OtpErlangObject errorInfo = result.elementAt(1);
        throw new MojoExecutionException("failed to scan expression: " + errorInfo.toString());
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (OtpErlangExit e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (OtpAuthException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * A wrapper function around {@link OtpSelf#connect(OtpPeer)} converting all
   * occuring exceptions into {@link MojoExecutionException}s.
   * 
   * @param self name of the node that will be created to connect from
   * @param peer node to connect to
   * @return a connection between the nodes that may be used for RPC
   *         communication
   * @throws MojoExecutionException
   */
  public static OtpConnection connect(String self, OtpPeer peer) throws MojoExecutionException {
    try {
      return new OtpSelf(self).connect(peer);
    }
    catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  /**
   * Converts the am array into a string containing a valid erlang list. The
   * elements will not be converted in any way.
   * 
   * @param array to convert, maybe {@code null}
   * @param p optional predicat whether to include a specific list element,
   *          maybe {@code null}
   * @return a string representing a valid erlang list
   */
  public static <T> String toPlainList(T[] array, Predicate<T> p) {
    if (array != null) {
      return toPlainList(Arrays.asList(array), p);
    }
    return "[]";
  }

  /**
   * Converts the a {@link Collection} into a string containing a valid erlang
   * list. The elements will not be converted in any way.
   * 
   * @param list to convert
   * @param p optional predicat whether to include a specific list element,
   *          maybe {@code null}
   * @return a string representing a valid erlang list
   */
  public static <T> String toPlainList(Collection<T> list, Predicate<T> p) {
    StringBuilder result = new StringBuilder("[");
    int i = 0;
    for (T elem : list) {
      if (p == null || p.pred(elem)) {
        if (i != 0) {
          result.append(", ");
        }
        result.append(elem.toString());
        i++;
      }
    }
    result.append("]");
    return result.toString();
  }

  /**
   * Converts the a {@link Collection} into a string containing a valid erlang
   * list. The elements will be converted into erlang strings.
   * 
   * @param array to convert, maybe {@code null}
   * @param p optional predicat whether to include a specific list element,
   *          maybe {@code null}
   * @return a string representing a valid erlang list
   */
  public static <T> String toStringList(T[] array, Predicate<T> p) {
    if (array != null) {
      return toStringList(Arrays.asList(array), p);
    }
    return "[]";
  }

  /**
   * Converts the a {@link Collection} into a string containing a valid erlang
   * list. The elements will be converted into erlang strings.
   * 
   * @param list to convert
   * @param p optional predicat whether to include a specific list element,
   *          maybe {@code null}
   * @return a string representing a valid erlang list
   */
  public static <T> String toStringList(Collection<T> list, Predicate<T> p) {
    StringBuilder result = new StringBuilder("[");
    int i = 0;
    for (T elem : list) {
      if (p == null || p.pred(elem)) {
        if (i != 0) {
          result.append(", ");
        }
        result.append("\"");
        result.append(elem.toString());
        result.append("\"");
        i++;
      }
    }
    result.append("]");
    return result.toString();
  }

  /**
   * Evaluate an erlang expression and return the result.
   * 
   * @param log logger.
   * @param expression the expression to evaluate.
   * @return the output of the erl interpreter.
   */
  public static String eval(Log log, String expression) throws MojoExecutionException, MojoFailureException {
    return eval(log, expression, null);
  }

  /**
   * Evaluate an erlang expression and return the result.
   * 
   * @param log logger
   * @param expression the expression to evaluate.
   * @param libPaths list of paths to add to the path, maybe {@code null}
   * @return the output of the erl interpreter.
   */
  public static String eval(Log log, String expression, List<File> libPaths) throws MojoExecutionException,
                                                                            MojoFailureException {
    return eval(log, expression, libPaths, null);
  }

  /**
   * Evaluate an erlang expression and return the result.
   * 
   * @param log logger
   * @param expression the expression to evaluate.
   * @param libPaths list of paths to add to the path, maybe {@code null}
   * @param workingDir the working directory for the spawned process, maybe
   *          {@code null}
   * @return the output of the erl interpreter.
   */
  public static String eval(final Log log, String expression, List<File> libPaths, File workingDir) throws MojoExecutionException,
                                                                                                   MojoFailureException {
    log.debug("Evaluating <<" + expression + ">>");
    final List<String> command = new LinkedList<String>();
    command.add(ErlConstants.ERL);
    if (libPaths != null) {
      for (File lib : libPaths) {
        command.add("-pa");
        command.add(lib.getAbsolutePath());
      }
    }
    command.add("-eval");
    command.add(expression);
    command.add("-noshell");
    command.add("-s");
    command.add("init");
    command.add("stop");

    return exec(command, log, workingDir, new Observer() {
      @Override
      public String handle(int exitValue, String result) throws MojoExecutionException {
        if (exitValue != 0) {
          log.error("Process returned: " + exitValue + " result is: " + result);
          throw new MojoExecutionException("Error evaluating expression " + command);
        }
        return result;
      }
    });
  }

  /**
   * Executes the given command array in the given working directory. After
   * process completion the given {@link Observer} is notfied to process the
   * result.
   * 
   * @param commands to execute
   * @param log used to log errors
   * @param workingDir the working directory for the spawned process, maybe
   *          {@code null}
   * @param observer to be notified on process completion
   */
  public static String exec(List<String> commands, final Log log, File workingDir, Observer observer) throws MojoExecutionException,
                                                                                                     MojoFailureException {
    log.debug("Executing " + commands.toString());
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(commands);
      processBuilder.directory(workingDir);
      log.debug("Working directory " + processBuilder.directory());
      Process process = processBuilder.start();
      final LinkedList<String> output = new LinkedList<String>();
      Thread gobbler1 = new Thread(new StreamGobbler(process.getInputStream(), new Processor() {
        @Override
        public void handle(String line) {
          log.info(line);
          output.addLast(line);
        }
      }));
      Thread gobbler2 = new Thread(new StreamGobbler(process.getErrorStream(), new Processor() {
        @Override
        public void handle(String line) {
          log.error(line);
        }
      }));
      gobbler1.start();
      gobbler2.start();
      process.waitFor();
      gobbler1.join();
      gobbler2.join();
      return observer.handle(process.exitValue(), output.isEmpty() ? null : output.getLast());
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    catch (InterruptedException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
