package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.erlang.MavenSelf.DEFAULT_PEER;
import static eu.lindenbaum.maven.util.ErlConstants.BEAM_SUFFIX;
import static eu.lindenbaum.maven.util.ErlConstants.ERL_SUFFIX;
import static eu.lindenbaum.maven.util.FileUtils.extractFilesFromJar;
import static eu.lindenbaum.maven.util.FileUtils.getDependencyIncludes;
import static eu.lindenbaum.maven.util.FileUtils.getFilesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.removeFilesRecursive;
import static eu.lindenbaum.maven.util.MavenUtils.getPluginFile;

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
 * Compile erlang test sources and recompile erlang sources with the options
 * {@code debug_info}, {@code export_all} and <code>{d, 'TEST'}</code>. This
 * will also compile the erlang sources provided along with the plugin.
 * 
 * @goal test-compile
 * @phase test-compile
 * @author Olivier Sambourg
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class TestCompileMojo extends AbstractErlangMojo {
  /**
   * Additional compiler options (comma separated) for test compilation that are
   * directly passed to <code>compile:file/2</code>, e.g. <code>"{d, Macro},
   * nowarn_unused_function"</code>. Note: The user may not specifiy one of the
   * {@code report} options since the {@link Mojo} itself uses the
   * {@code return} option internally. Warnings and Errors will be printed
   * without specifying extra options.
   * 
   * @parameter expression="${testCompilerOptions}"
   */
  private String testCompilerOptions;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
    this.targetTest.mkdirs();
    int removed = removeFilesRecursive(this.targetTest, BEAM_SUFFIX);
    log.debug("Removed " + removed + " stale " + BEAM_SUFFIX + "-files from " + this.targetTest);

    List<File> files = getFilesRecursive(this.srcTestErlang, ERL_SUFFIX);
    if (!files.isEmpty()) {
      File plugin = getPluginFile("maven-erlang-plugin", this.project, this.repository);
      extractFilesFromJar(plugin, ERL_SUFFIX, this.targetTest);

      files.addAll(getFilesRecursive(this.srcMainErlang, ERL_SUFFIX));
      files.add(new File(this.targetTest, "mock.erl"));
      files.add(new File(this.targetTest, "surefire.erl"));
      files.add(new File(this.targetTest, "cover2.erl"));

      List<File> includes = new ArrayList<File>();
      includes.addAll(getDependencyIncludes(this.targetLib));
      includes.add(this.srcMainInclude);
      includes.add(this.srcTestInclude);
      includes.add(this.targetInclude);
      includes.add(this.srcMainErlang);

      List<String> options = new ArrayList<String>();
      options.add("debug_info");
      options.add("export_all");
      options.add("{d, 'TEST'}");
      if (this.testCompilerOptions != null && !this.testCompilerOptions.isEmpty()) {
        log.info("Using additinal test compiler options: " + this.testCompilerOptions);
        options.add(this.testCompilerOptions);
      }

      Script<CompilerResult> script = new BeamCompilerScript(files, this.targetTest, includes, options);
      CompilerResult result = MavenSelf.get().eval(DEFAULT_PEER, script);
      result.logOutput(log);
      String failedCompilationUnit = result.getFailed();
      if (failedCompilationUnit != null) {
        throw new MojoFailureException("failed to compile " + failedCompilationUnit);
      }
      log.info("Successfully compiled " + files.size() + " test source file(s).");
    }
    else {
      log.info("No test source files to compile.");
    }
  }
}
