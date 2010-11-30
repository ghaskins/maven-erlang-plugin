package eu.lindenbaum.maven.mojo;

import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.removeDirectory;
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
 * @goal copy-test-resources
 * @phase generate-test-resources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class CopyTestResources extends ErlangMojo {
  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
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
