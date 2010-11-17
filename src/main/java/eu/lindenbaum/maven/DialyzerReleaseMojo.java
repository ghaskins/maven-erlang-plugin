package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.erlang.MavenSelf.DEFAULT_PEER;
import static eu.lindenbaum.maven.util.ErlConstants.DIALYZER_OK;
import static eu.lindenbaum.maven.util.FileUtils.getDependencyIncludes;
import static eu.lindenbaum.maven.util.FileUtils.newerFilesThan;
import static eu.lindenbaum.maven.util.MavenUtils.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import eu.lindenbaum.maven.erlang.DialyzerScript;
import eu.lindenbaum.maven.erlang.MavenSelf;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * <p>
 * This {@link Mojo} runs the erlang {@code dialyzer} tool on a complete release
 * found in {@link AbstractErlangMojo#targetLib}. The {@code dialyzer} can be
 * skipped using the {@code skipDialyzer} paramter in the projects pom. Since
 * this {@link Mojo} is called in order to check a complete release it is run
 * over all release dependencies.
 * </p>
 * 
 * @goal dialyzer-release
 * @phase compile
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class DialyzerReleaseMojo extends AbstractErlangMojo {
  /**
   * Setting this to {@code true} will skip the {@code dialyzer} analysis.
   * 
   * @parameter expression="${skipDialyzer}" default-value=false
   */
  private boolean skipDialyzer;

  /**
   * Setting this to {@code true} will break the build when a {@code dialyzer}
   * run returns warnings.
   * 
   * @parameter expression="${dialyzerWarningsAreErrors}" default-value=false
   */
  private boolean dialyzerWarningsAreErrors;

  /**
   * Additional {@code dialyzer} warning options. This must be a comma separated
   * list with valid warning atoms that will be included when calling
   * <code>dialyzer:run([{warnings,[...]}, ...])</code>.
   * 
   * @parameter expression="${dialyzerOptions}"
   * @see http://www.erlang.org/doc/man/dialyzer.html
   */
  private String dialyzerOptions;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    Log log = getLog();
    log.info(SEPARATOR);
    log.info(" D I A L Y Z E");
    log.info(SEPARATOR);
    if (this.skipDialyzer) {
      log.warn("Dialyzer is configured to be skipped.");
    }
    else {
      File lastBuildIndicator = new File(this.target, DIALYZER_OK);
      if (newerFilesThan(this.targetLib, lastBuildIndicator)) {
        lastBuildIndicator.delete();
        log.info("Running dialyzer on " + this.targetLib);

        List<File> sources = Arrays.asList(new File[]{ this.targetLib });
        List<File> includes = getDependencyIncludes(this.targetLib);

        DialyzerScript script = new DialyzerScript(sources, includes, this.dialyzerOptions);
        String[] warnings = MavenSelf.get().eval(DEFAULT_PEER, script);
        for (String warning : warnings) {
          log.warn(warning);
        }
        if (warnings.length > 0 && this.dialyzerWarningsAreErrors) {
          throw new MojoFailureException("dialyzer emitted warnings");
        }
        log.info("Dialyzer run successful.");

        try {
          lastBuildIndicator.createNewFile();
        }
        catch (IOException e) {
          throw new MojoExecutionException("failed to create " + lastBuildIndicator);
        }
      }
      else {
        log.info("Last dialyzer run is still up to date.");
      }
    }
  }
}
