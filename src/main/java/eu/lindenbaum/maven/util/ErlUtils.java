package eu.lindenbaum.maven.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
   * Converts an array into a string containing a valid erlang list. The
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
   * Converts a {@link Collection} into a string containing a valid erlang list.
   * The elements will not be converted in any way.
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
   * Converts a {@link Collection} into a string containing a valid erlang list.
   * The elements will be converted into erlang strings.
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
   * Converts a {@link Collection} into a string containing a valid erlang list.
   * The elements will be converted into erlang strings.
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
   * Converts a {@link Collection} of files into a string containing a valid
   * erlang list. The files will be converted into erlang strings using
   * {@link File#getAbsolutePath()}. The files will be checked for {@code null}
   * and existence. The prefix and postfix {@link String}s will be
   * prepended/appended to every element of the list.
   * 
   * @param list to convert
   * @param prefix to prepend to an entry
   * @param postfix to append to an entry
   * @return a string representing a valid erlang list
   */
  public static String toFileList(Collection<File> list, String prefix, String postfix) {
    StringBuilder result = new StringBuilder("[");
    int i = 0;
    for (File file : list) {
      if (file != null && file.exists()) {
        if (i != 0) {
          result.append(", ");
        }
        result.append(prefix);
        result.append("\"");
        result.append(file.getAbsolutePath());
        result.append("\"");
        result.append(postfix);
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
