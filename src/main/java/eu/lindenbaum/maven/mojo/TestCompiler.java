package eu.lindenbaum.maven.mojo;

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
import eu.lindenbaum.maven.util.ErlConstants;
import eu.lindenbaum.maven.util.MavenUtils;

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
public final class TestCompiler extends ErlangMojo {
  /**
   * Setting this to {@code true will} will skip the test compilation.
   * 
   * @parameter expression="${skipTests}" default-value=false
   */
  private boolean skipTests;

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
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    log.info(MavenUtils.SEPARATOR);
    log.info(" T E S T - C O M P I L E R");
    log.info(MavenUtils.SEPARATOR);

    if (this.skipTests) {
      log.info("Test compilation is skipped.");
      return;
    }

    p.targetTest().mkdirs();
    int removed = removeFilesRecursive(p.targetTest(), ErlConstants.BEAM_SUFFIX);
    log.debug("Removed " + removed + " stale " + ErlConstants.BEAM_SUFFIX + "-files from " + p.targetTest());

    List<File> files = getFilesRecursive(p.test_src(), ErlConstants.ERL_SUFFIX);
    if (!files.isEmpty()) {
      File plugin = getPluginFile("maven-erlang-plugin", p.project(), p.repository());
      extractFilesFromJar(plugin, ErlConstants.ERL_SUFFIX, p.targetTest());

      files.addAll(getFilesRecursive(p.src(), ErlConstants.ERL_SUFFIX));
      files.add(new File(p.targetTest(), "mock.erl"));
      files.add(new File(p.targetTest(), "surefire.erl"));
      files.add(new File(p.targetTest(), "cover2.erl"));

      List<File> includes = new ArrayList<File>();
      includes.addAll(getDependencyIncludes(p.targetLib()));
      includes.add(p.include());
      includes.add(p.test_include());
      includes.add(p.targetInclude());
      includes.add(p.src());

      List<String> options = new ArrayList<String>();
      options.add("debug_info");
      options.add("export_all");
      options.add("{d, 'TEST'}");
      if (this.testCompilerOptions != null && !this.testCompilerOptions.isEmpty()) {
        log.info("Using additinal test compiler options: " + this.testCompilerOptions);
        options.add(this.testCompilerOptions);
      }

      Script<CompilerResult> script = new BeamCompilerScript(files, p.targetTest(), includes, options);
      CompilerResult result = MavenSelf.get().eval(p.node(), script);
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
