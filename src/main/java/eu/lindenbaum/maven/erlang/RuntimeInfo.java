package eu.lindenbaum.maven.erlang;

import java.io.File;

/**
 * Interface representing the result returned by the {@link RuntimeInfoScript}.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public interface RuntimeInfo {
  /**
   * Returns the erlang runtime's library directory as returned by
   * <code>code:lib_dir()</code>.
   * 
   * @return The erlang runtime's library directory.
   */
  public File libDirectory();
}
