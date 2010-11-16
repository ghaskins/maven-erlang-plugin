package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.erlang.MavenSelf.DEFAULT_PEER;
import static eu.lindenbaum.maven.util.ErlConstants.BEAM_SUFFIX;
import static eu.lindenbaum.maven.util.ErlConstants.ERL_SUFFIX;
import static eu.lindenbaum.maven.util.FileUtils.getDependencyIncludes;
import static eu.lindenbaum.maven.util.FileUtils.getFilesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.removeFilesRecursive;
import static eu.lindenbaum.maven.util.MavenUtils.SEPARATOR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.lindenbaum.maven.erlang.BeamCompilerScript;
import eu.lindenbaum.maven.erlang.CompilerResult;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;

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
 */
public final class CompileMojo extends AbstractErlangMojo {
  /**
   * Additional compiler options (comma separated) for compilation that are
   * directly passed to <code>compile:file/2</code>, e.g. <code>"debug_info,
   * nowarn_unused_function"</code>. Note: The user may not specifiy one of the
   * {@code report} options since the {@link Mojo} itself uses the
   * {@code return} option internally. Warnings and Errors will be printed
   * without specifying extra options.
   * 
   * @parameter expression=${compilerOptions}
   */
  private String compilerOptions;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
    log.info(SEPARATOR);
    log.info(" C O M P I L E");
    log.info(SEPARATOR);

    this.targetEbin.mkdirs();
    int removed = removeFilesRecursive(this.targetEbin, BEAM_SUFFIX);
    log.debug("Removed " + removed + " stale " + BEAM_SUFFIX + "-files from " + this.targetEbin);

    List<File> files = getFilesRecursive(this.srcMainErlang, ERL_SUFFIX);
    if (!files.isEmpty()) {
      List<File> includes = new ArrayList<File>();
      includes.addAll(getDependencyIncludes(this.targetLib));
      includes.add(this.srcMainInclude);
      includes.add(this.targetInclude);
      includes.add(this.srcMainErlang);

      List<String> options = new ArrayList<String>();
      if (this.compilerOptions != null) {
        log.info("Using additional compiler options: " + this.compilerOptions);
        options.add(this.compilerOptions);
      }

      Script<CompilerResult> script = new BeamCompilerScript(files, this.targetEbin, includes, options);
      CompilerResult result = MavenSelf.get().eval(DEFAULT_PEER, script);
      result.logOutput(log);
      String failedCompilationUnit = result.getFailed();
      if (failedCompilationUnit != null) {
        throw new MojoFailureException("failed to compile " + failedCompilationUnit);
      }
      log.info("Successfully compiled " + files.size() + " source file(s).");
    }
    else {
      log.info("No source files to compile.");
    }
  }
}
