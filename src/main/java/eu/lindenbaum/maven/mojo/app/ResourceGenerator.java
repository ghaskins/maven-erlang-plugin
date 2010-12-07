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
 * Copies all resource files into that target directory structure. Copied
 * resources contain:
 * <ul>
 * <li>erlang source files (*.erl)</li>
 * <li>erlang include files (*.hrl)</li>
 * <li>resources (*)</li>
 * </ul>
 * 
 * @goal generate-resources
 * @phase generate-resources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class ResourceGenerator extends ErlangMojo {
  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    removeDirectory(p.targetProject());

    int sources = 0;
    sources += copyDirectory(p.src(), p.targetSrc(), FileUtils.SOURCE_FILTER);
    log.debug("copied " + sources + " sources");
    if (sources == 0) {
      p.targetSrc().delete();
    }

    int includes = 0;
    includes += copyDirectory(p.include(), p.targetInclude(), FileUtils.SOURCE_FILTER);
    log.debug("copied " + includes + " includes");
    if (includes == 0) {
      p.targetInclude().delete();
    }

    int resources = 0;
    resources += copyDirectory(p.priv(), p.targetPriv(), FileUtils.NULL_FILTER);
    log.debug("copied " + resources + " resources");
    if (resources == 0) {
      p.targetPriv().delete();
    }

    // TODO get other dependencies and copy to priv dir
  }
}
