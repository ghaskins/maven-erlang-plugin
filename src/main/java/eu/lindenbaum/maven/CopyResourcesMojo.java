package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.util.ErlConstants.SRC_SUFFIX;
import static eu.lindenbaum.maven.util.FileUtils.NULL_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.SNMP_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.SOURCE_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.getFilesAndDirectoriesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.removeDirectory;

import java.io.File;
import java.io.FileFilter;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * <p>
 * This {@link Mojo} copies the private resources to the
 * {@link AbstractErlangMojo#targetPriv} directory. Default source folders for
 * private resources are {@link AbstractErlangMojo#srcMainPriv} and
 * {@link AbstractErlangMojo#srcMainResourcesPriv}.
 * </p>
 * <p>
 * TODO Documentation
 * </p>
 * <p>
 * TODO edoc resources
 * </p>
 * 
 * @goal copy-resources
 * @phase generate-resources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class CopyResourcesMojo extends AbstractErlangMojo {
  @Override
  public void execute() throws MojoExecutionException {
    Log log = getLog();
    removeDirectory(this.targetProject);

    int sources = 0;
    sources += copyDirectory(this.srcMainErlang, this.targetSrc, SOURCE_FILTER);
    log.debug("copied " + sources + " sources");
    if (sources == 0) {
      this.targetSrc.delete();
    }

    int includes = 0;
    includes += copyDirectory(this.srcMainInclude, this.targetInclude, SOURCE_FILTER);
    log.debug("copied " + includes + " includes");
    if (includes == 0) {
      this.targetInclude.delete();
    }

    int snmp = 0;
    snmp += copyDirectory(this.srcMainErlang, this.targetMibs, SNMP_FILTER);
    log.debug("copied " + snmp + " snmp resources");
    if (snmp == 0) {
      this.targetMibs.delete();
    }

    int resources = 0;
    resources += copyDirectory(this.srcMainPriv, this.targetPriv, NULL_FILTER);
    resources += copyDirectory(this.srcMainResourcesPriv, this.targetPriv, NULL_FILTER);
    log.debug("copied " + resources + " resources");
    if (resources == 0) {
      this.targetPriv.delete();
    }

    // package non erlang source folders, e.g. c, java, ... into c_src, java_src, ...
    FileFilter filter = new FileFilter() {
      @Override
      public boolean accept(File dir) {
        return dir.isDirectory() //
               && !dir.equals(CopyResourcesMojo.this.srcMainErlang) //
               && !dir.equals(CopyResourcesMojo.this.srcMainInclude) //
               && !dir.equals(CopyResourcesMojo.this.srcMainPriv) //
               && !dir.equals(CopyResourcesMojo.this.srcMainResources);
      }
    };
    for (File source : getFilesAndDirectoriesRecursive(this.srcMain, filter)) {
      int other = 0;
      File destination = new File(this.targetProject, source.getName() + SRC_SUFFIX);
      other += copyDirectory(source, destination, NULL_FILTER);
      log.debug("copied " + other + " non-erlang sources");
      if (other == 0) {
        destination.delete();
      }
    }
  }
}
