package eu.lindenbaum.maven.mojo.app;

import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.removeDirectory;
import eu.lindenbaum.maven.ErlangMojo;
import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.util.FileUtils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Copies all test resource files into that target directory structure. Copied
 * resources contain:
 * <ul>
 * <li>resources (*)</li>
 * <li>test resources (*)</li>
 * </ul>
 * 
 * @goal generate-test-resources
 * @phase generate-test-resources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class TestResourceGenerator extends ErlangMojo {
  /**
   * Setting this to {@code true will} will skip copying the test resources.
   * 
   * @parameter expression="${skipTests}" default-value=false
   */
  private boolean skipTests;

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    if (this.skipTests) {
      return;
    }

    removeDirectory(p.targetTest());

    int testResources = 0;
    testResources += copyDirectory(p.priv(), p.targetTestPriv(), FileUtils.NULL_FILTER);
    testResources += copyDirectory(p.test_priv(), p.targetTestPriv(), FileUtils.NULL_FILTER);
    log.debug("copied " + testResources + " test resources");
    if (testResources == 0) {
      p.targetTest().delete();
    }
  }
}
