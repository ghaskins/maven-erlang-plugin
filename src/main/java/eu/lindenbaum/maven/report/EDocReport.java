package eu.lindenbaum.maven.report;

import java.io.File;
import java.util.Locale;

import eu.lindenbaum.maven.ErlangReport;
import eu.lindenbaum.maven.PackagingType;
import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.erlang.EDocScript;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;
import eu.lindenbaum.maven.util.MavenUtils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.MavenReportException;

/**
 * This {@link Mojo} will generate source code documentation for an application.
 * 
 * @goal edoc
 * @execute phase="generate-resources"
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public class EDocReport extends ErlangReport {
  @Override
  public String getOutputName() {
    return "edoc/index";
  }

  @Override
  public String getName(Locale locale) {
    return "edoc";
  }

  @Override
  public String getDescription(Locale locale) {
    return "Source code documentation of the project.";
  }

  @Override
  public boolean isExternalReport() {
    return true;
  }

  /**
   * Returns whether this report can generate any output.
   * 
   * @return {@code true} if the project is an application, {@code false}
   *         otherwise
   */
  @Override
  public boolean canGenerateReport() {
    PackagingType type = PackagingType.fromString(getProject().getPackaging());
    return type == PackagingType.ERLANG_OTP || type == PackagingType.ERLANG_STD;
  }

  @Override
  protected void execute(Log log, Locale l, Properties p) throws MojoExecutionException,
                                                         MojoFailureException,
                                                         MavenReportException {
    log.info(MavenUtils.SEPARATOR);
    log.info(" E D O C");
    log.info(MavenUtils.SEPARATOR);

    if (!canGenerateReport()) {
      log.info("Nothing to do.");
      return;
    }

    String application = p.project().getArtifactId();
    File outdir = new File(getReportOutputDirectory(), "edoc");
    File overview = new File(p.target(), "overview.edoc");

    outdir.mkdirs();
    Script<Boolean> script = new EDocScript(application, p.src(), outdir, overview);
    Boolean success = MavenSelf.get().exec(p.node(), script);
    if (!success) {
      throw new MojoExecutionException("failed to generate documentation");
    }
    else {
      log.info("Successfully generated documentation.");
    }
  }
}
