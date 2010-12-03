package eu.lindenbaum.maven.erlang;

import org.apache.maven.plugin.logging.Log;

/**
 * Interface representing the result returned by the {@link MakeScriptScript}.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public interface MakeScriptResult {
  /**
   * Returns whether generation of scripts succeeded.
   * 
   * @return {@code true} if generation succeeded, {@code false} otherwise.
   */
  public boolean success();

  /**
   * Log the test output (e.g. infos/warnings/errors) using the provided logger.
   * 
   * @param log used to print the output
   */
  public void logOutput(Log log);
}
