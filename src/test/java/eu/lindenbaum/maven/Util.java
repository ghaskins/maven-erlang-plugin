package eu.lindenbaum.maven;

import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.mojo.StartBackend;

/**
 * Utility for unit tests needing a running erlang backend node.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class Util {
  public static String PEER = MavenSelf.DEFAULT_PEER;

  /**
   * Starts a detached node. A connection to that node can be established using
   * the public {@link #PEER} container.
   */
  public static void startBackendNode() {
    try {
      new StartBackend().execute();
    }
    catch (Exception e) {
      // ignored
    }
  }
}
