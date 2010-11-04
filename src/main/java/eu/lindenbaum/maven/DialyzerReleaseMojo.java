package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.util.ErlConstants.DIALYZER_OK;
import static eu.lindenbaum.maven.util.FileUtils.getDependencyIncludes;
import static eu.lindenbaum.maven.util.FileUtils.newerFilesThan;
import static eu.lindenbaum.maven.util.MavenUtils.SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * <p>
 * This {@link Mojo} runs the erlang {@code dialyzer} tool on a complete release
 * found in {@link AbstractErlangMojo#targetLib}. The {@code dialyzer} can be
 * skipped using the {@code useDialyzer} paramter in the projects pom. Since
 * this {@link Mojo} is called in order to check a complete release it is run
 * over all release dependencies.
 * </p>
 * 
 * @goal dialyzer-release
 * @phase compile
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class DialyzerReleaseMojo extends AbstractDialyzerMojo {
  /**
   * Setting this to {@code true} will skip the {@code dialyzer} analysis.
   * 
   * @parameter default-value=false
   */
  private boolean skipDialyzer;

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

        executeDialyzer(sources, includes);
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
