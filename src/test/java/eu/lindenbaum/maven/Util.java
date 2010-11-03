package eu.lindenbaum.maven;

import com.ericsson.otp.erlang.OtpPeer;

/**
 * Utility for unit tests needing a running erlang backend node.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class Util {
  public static OtpPeer PEER = AbstractErlangMojo.peer;

  /**
   * Starts a detached node. A connection to that node can be established using
   * the public {@link #PEER} container.
   */
  public static void startBackendNode() {
    try {
      new StartBackendMojo().execute();
    }
    catch (Exception e) {
      // ignored
    }
  }
}
