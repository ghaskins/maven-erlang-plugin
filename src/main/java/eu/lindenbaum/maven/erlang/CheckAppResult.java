package eu.lindenbaum.maven.erlang;

/**
 * Interface representing the result returned by the {@link CheckAppFileScript}.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public interface CheckAppResult {
  /**
   * Returns the application name as stated in the {@code .app} file.
   * 
   * @return The application name or {@code "undefined"} if not present.
   */
  public String getName();

  /**
   * Returns the application version as stated in the {@code .app} file.
   * 
   * @return The application version or {@code "undefined"} if not present.
   */
  public String getVersion();

  /**
   * Returns the applications start module as stated in the {@code .app} file.
   * 
   * @return The start module name or {@code "undefined"} if not present.
   */
  public String getStartModule();

  /**
   * Returns a list of the modules configured in the {@code .app} file.
   * 
   * @return A non-{@code null} list containing the configured applications
   *         modules.
   */
  public String[] getModules();

  /**
   * Returns a list of the applications that must be running before the
   * application can start as stated in the {@code .app} file.
   * 
   * @return A non-{@code null} list containing the application dependencies.
   */
  public String[] getApplications();
}
