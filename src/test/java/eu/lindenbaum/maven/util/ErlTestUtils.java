package eu.lindenbaum.maven.util;

import java.util.ArrayList;

import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpPeer;

/**
 * Utility for unit tests needing a running erlang backend node.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
final class ErlTestUtils {
  public static OtpPeer TEST_PEER = new OtpPeer("erl-utils-test-backend");

  /**
   * Starts a detached node. A connection to that node can be established using
   * the public {@link #TEST_PEER} container.
   */
  public static void startBackendNode() {
    ArrayList<String> command = new ArrayList<String>();
    command.add(ErlConstants.ERL);
    command.add("-sname");
    command.add(TEST_PEER.alive());
    command.add("-detached");
    try {
      new ProcessBuilder(command).start().waitFor();
    }
    catch (Exception e) {
      // ignored
    }
  }

  /**
   * Stops the erlang backend node started with {@link #startBackendNode()}.
   */
  public static void stopBackendNode() {
    try {
      OtpConnection connection = MavenSelf.get().connect(TEST_PEER);
      connection.sendRPC("erlang", "halt", new OtpErlangList());
    }
    catch (Exception e) {
      // ignored
    }
  }
}
