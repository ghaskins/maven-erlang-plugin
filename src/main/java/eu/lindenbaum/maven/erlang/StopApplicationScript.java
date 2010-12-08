package eu.lindenbaum.maven.erlang;

import java.io.File;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangObject;

import eu.lindenbaum.maven.util.ErlUtils;

/**
 * A {@link Script} stopping a list of erlang applications as well as cleaning
 * the loaded modules and code paths.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public class StopApplicationScript implements Script<Void> {
  private static final String script = //
  "    CodePaths = %s, Modules = %s, ToPreserve = %s, " + //
      "[code:del_path(P) || P <- CodePaths]," + //
      "After = [A || {A, _, _} <- application:which_applications()]," + //
      "[application:stop(A) || A <- After -- ToPreserve]," + //
      "[application:unload(A) || A <- After -- ToPreserve]," + //
      "[code:purge(M) || M <- Modules]," + //
      "[code:delete(M) || M <- Modules]," + //
      "[code:purge(M) || M <- Modules].";

  private final List<File> codePaths;
  private final List<File> modules;
  private final List<String> preservedApplications;

  /**
   * Creates a {@link Script} that stops all applications except a specific list
   * of applications.
   * 
   * @param codePaths to remove
   * @param modules to purge
   * @param preservedApplications to not stop
   */
  public StopApplicationScript(List<File> codePaths, List<File> modules, List<String> preservedApplications) {
    this.codePaths = codePaths;
    this.modules = modules;
    this.preservedApplications = preservedApplications;
  }

  @Override
  public String get() {
    String excluded = ErlUtils.toList(this.preservedApplications, null, "'", "'");
    String modules = ErlUtils.toModuleList(this.modules, "'", "'");
    String paths = ErlUtils.toFileList(this.codePaths, "\"", "\"");
    return String.format(script, paths, modules, excluded);
  }

  /**
   * The result of the {@link Script} execution is ignored.
   * 
   * @param the result term of the {@link Script} execution
   * @return Always {@code null}.
   */
  @Override
  public Void handle(OtpErlangObject result) {
    return null;
  }
}
