package eu.lindenbaum.maven.erlang;

import java.util.List;

/**
 * Interface representing the result returned by the
 * {@link StartApplicationScript}.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public interface StartResult {
  /**
   * Returns whether the application could be started successfully.
   * 
   * @return {@code true} whether the application was started successfully,
   *         {@code false} otherwise.
   */
  public boolean startSucceeded();

  /**
   * Returns a list of applications that were already running before the
   * application was started.
   * 
   * @return A non-{@code null} list of applications that were started already.
   */
  public List<String> getBeforeApplications();
}
