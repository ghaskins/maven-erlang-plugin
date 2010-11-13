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
import java.util.Arrays;
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
public final class CompileMojo extends AbstractCompilerMojo {
  /**
   * Setting this to {@code true} will compile all modules with debug
   * information.
   * 
   * @parameter expression=${debugInfo} default-value=false
   */
  private boolean debugInfo;

  /**
   * Additional compiler options.
   * 
   * @parameter expression=${compilerOptions}
   */
  private String[] compilerOptions;

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
      includes.addAll(Arrays.asList(new File[]{ this.srcMainInclude, this.targetInclude, this.srcMainErlang }));
      includes.addAll(getDependencyIncludes(this.targetLib));
      List<String> options = new ArrayList<String>();
      if (this.compilerOptions != null) {
        options.addAll(Arrays.asList(this.compilerOptions));
      }
      if (this.debugInfo) {
        options.add("+debug_info");
      }

      Script<CompilerResult> script = new BeamCompilerScript(files, this.targetEbin, includes, options);
      CompilerResult result = MavenSelf.get().eval(DEFAULT_PEER, script);
      result.logOutput(log);
      String failedCompilationUnit = result.getFailed();
      if (failedCompilationUnit != null) {
        throw new MojoFailureException("failed to compile " + failedCompilationUnit);
      }
      log.info("Compilation successfull.");
    }
    else {
      log.info("No sources to compile.");
    }
  }
}
