package eu.lindenbaum.maven.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

/**
 * A wrapper around an {@link OtpSelf} node that acts as a connection cache for
 * destination erlang nodes. Instantiation is guarded by the singleton pattern.
 * To retrieve an instance call {@link MavenSelf#get()}. Connections retrieved
 * by {@link MavenSelf#connect(OtpPeer)} are cached in order to return an
 * already established connection. Thus this method can be called multiple
 * times.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class MavenSelf {
  private static final Object lock = new Object();

  private static MavenSelf instance = null;

  private final OtpSelf self;
  private final Map<OtpPeer, OtpConnection> connections;

  private MavenSelf(OtpSelf self) {
    this.self = self;
    this.connections = new HashMap<OtpPeer, OtpConnection>();
  }

  /**
   * Returns a unique instance of {@link MavenSelf} using the singleton pattern.
   * 
   * @return an instance of {@link MavenSelf}, never {@code null}
   * @throws MojoExecutionException in case the instance cannot be created
   */
  public static MavenSelf get() throws MojoExecutionException {
    synchronized (lock) {
      if (instance == null) {
        try {
          OtpSelf otpSelf = new OtpSelf("maven-erlang-plugin-frontend");
          instance = new MavenSelf(otpSelf);
        }
        catch (IOException e) {
          throw new MojoExecutionException("failed to create self node", e);
        }
      }
      return instance;
    }
  }

  /**
   * Establishes an {@link OtpConnection} between this node and a specific
   * {@link OtpPeer}. The returned connection may be an already existing, cached
   * connection.
   * 
   * @param peer to connect to
   * @return an {@link OtpConnection} that may be used for rpc communication
   * @throws MojoExecutionException in case the connection cannot be established
   */
  public OtpConnection connect(OtpPeer peer) throws MojoExecutionException {
    synchronized (lock) {
      OtpConnection connection = this.connections.get(peer);
      if (connection == null) {
        try {
          for (int i = 0; i < 6; ++i) {
            try {
              connection = this.self.connect(peer);
              this.connections.put(peer, connection);
              break;
            }
            catch (IOException e) {
              Thread.sleep(500L);
            }
          }
        }
        catch (OtpAuthException e) {
          throw new MojoExecutionException("failed to connect to " + peer);
        }
        catch (InterruptedException e) {
          throw new MojoExecutionException("failed to connect to " + peer);
        }
        if (connection == null) {
          throw new MojoExecutionException("failed to connect to " + peer);
        }
      }
      return connection;
    }
  }

  /**
   * Executes an erlang script on a specific remote erlang node using RPC. A
   * connection to the remote node will be established if necessary.
   * 
   * @param peer to evaluate the expression on
   * @param expression to evaluate
   * @return the result term of the expression
   * @throws MojoExecutionException
   */
  public OtpErlangObject eval(OtpPeer peer, String expression) throws MojoExecutionException {
    OtpConnection connection = connect(peer);
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
}
