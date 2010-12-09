package eu.lindenbaum.maven.erlang;

import java.io.File;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangObject;

import eu.lindenbaum.maven.util.ErlUtils;

/**
 * A {@link Script} that loads a list of modules located in the provided code
 * paths. All modules will be purged before loading to prevent module version
 * conflicts. The provided code paths will be removed from the backend path
 * after module loading.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class LoadModulesScript implements Script<Void> {
  private static final String script = //
  "    CodePaths = %s," + //
      "Modules = %s," + //
      "code:add_pathsa(CodePaths)," + //
      "lists:foreach(" + //
      "  fun(Module) ->" + //
      "        code:purge(Module)," + //
      "        code:delete(Module)," + //
      "        code:purge(Module)," + //
      "        code:load_file(Module)" + //
      "  end, Modules)," + //
      "[code:del_path(P) || P <- CodePaths].";

  private final List<File> modules;
  private final List<File> codePaths;

  public LoadModulesScript(List<File> modules, List<File> codePaths) {
    this.modules = modules;
    this.codePaths = codePaths;
  }

  @Override
  public String get() {
    String paths = ErlUtils.toFileList(this.codePaths, "\"", "\"");
    String modules = ErlUtils.toModuleList(this.modules, "'", "'");
    return String.format(script, paths, modules);
  }

  /**
   * The result of the {@link Script} execution is ignored.
   */
  @Override
  public Void handle(OtpErlangObject result) {
    return null;
  }
}
