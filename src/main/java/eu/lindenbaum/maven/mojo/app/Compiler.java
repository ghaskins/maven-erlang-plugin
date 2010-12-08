package eu.lindenbaum.maven.mojo.app;

import static eu.lindenbaum.maven.util.FileUtils.getDirectoriesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.getFilesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.removeFilesRecursive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.lindenbaum.maven.ErlangMojo;
import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.erlang.BeamCompilerScript;
import eu.lindenbaum.maven.erlang.CompilerResult;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;
import eu.lindenbaum.maven.util.ErlConstants;
import eu.lindenbaum.maven.util.MavenUtils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * This {@link Mojo} compiles the projects erlang sources.
 * 
 * @goal compile
 * @phase compile
 * @author Olivier Sambourg
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class Compiler extends ErlangMojo {
  /**
   * Additional compiler options (comma separated) for compilation that are
   * directly passed to <code>compile:file/2</code>, e.g. <code>"debug_info,
   * nowarn_unused_function"</code>. Note: The user may not specifiy one of the
   * {@code report} options since the {@link Mojo} itself uses the
   * {@code return} option internally. Warnings and Errors will be printed
   * without specifying extra options.
   * 
   * @parameter expression="${compilerOptions}"
   */
  private String compilerOptions;

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    log.info(MavenUtils.SEPARATOR);
    log.info(" C O M P I L E R");
    log.info(MavenUtils.SEPARATOR);

    p.targetEbin().mkdirs();
    int removed = removeFilesRecursive(p.targetEbin(), ErlConstants.BEAM_SUFFIX);
    log.debug("Removed " + removed + " stale " + ErlConstants.BEAM_SUFFIX + "-files from " + p.targetEbin());

    List<File> files = getFilesRecursive(p.src(), ErlConstants.ERL_SUFFIX);
    if (!files.isEmpty()) {
      List<File> includes = new ArrayList<File>();
      includes.addAll(getDirectoriesRecursive(p.targetLib(), ErlConstants.HRL_SUFFIX));
      includes.add(p.include());
      includes.add(p.targetInclude());
      includes.add(p.src());

      List<String> options = new ArrayList<String>();
      if (this.compilerOptions != null && !this.compilerOptions.isEmpty()) {
        log.info("Using additional compiler options: " + this.compilerOptions);
        options.add(this.compilerOptions);
      }

      Script<CompilerResult> script = new BeamCompilerScript(files, p.targetEbin(), includes, options);
      List<File> codePaths = getDirectoriesRecursive(p.targetLib(), ErlConstants.BEAM_SUFFIX);
      CompilerResult result = MavenSelf.get().eval(p.node(), script, codePaths);
      result.logOutput(log);
      String failedCompilationUnit = result.getFailed();
      if (failedCompilationUnit != null) {
        throw new MojoFailureException("Failed to compile " + failedCompilationUnit + ".");
      }
      log.info("Successfully compiled " + files.size() + " source file(s).");
    }
    else {
      log.info("No source files to compile.");
    }
  }
}
