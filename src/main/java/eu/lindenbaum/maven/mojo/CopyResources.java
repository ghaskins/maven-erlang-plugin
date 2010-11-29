package eu.lindenbaum.maven.mojo;

import static eu.lindenbaum.maven.util.ErlConstants.SRC_SUFFIX;
import static eu.lindenbaum.maven.util.FileUtils.NULL_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.SOURCE_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.getFilesAndDirectoriesRecursive;
import static eu.lindenbaum.maven.util.FileUtils.removeDirectory;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;

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
 * <li>test resources (*)</li>
 * <li>foreign source folders (e.g. c, java, ... sources)</li>
 * </ul>
 * 
 * @goal copy-resources
 * @phase generate-resources
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class CopyResources extends ErlangMojo {
  /**
   * A foreign source filter for projects with standard maven packaging. Matches
   * every folder contained in the collection of supported languages.
   */
  static final FileFilter otpFilter = new FileFilter() {
    private final Collection<String> supported = Arrays.asList("beanshell",
                                                               "c",
                                                               "cpp",
                                                               "c++",
                                                               "groovy",
                                                               "haskell",
                                                               "java",
                                                               "python",
                                                               "ruby");

    @Override
    public boolean accept(File dir) {
      return dir.isDirectory() && this.supported.contains(dir.getName());
    }
  };

  /**
   * A foreign source filter for projects with standard erlang/OTP packaging.
   * Matches every folder ending with {@code "_src"} that does not contain
   * {@code "test"}.
   */
  static final FileFilter stdFilter = new FileFilter() {
    @Override
    public boolean accept(File dir) {
      return dir.isDirectory() && dir.getName().endsWith(SRC_SUFFIX) && !dir.getName().contains("test");
    }
  };

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    removeDirectory(p.targetProject());

    int sources = 0;
    sources += copyDirectory(p.src(), p.targetSrc(), SOURCE_FILTER);
    log.debug("copied " + sources + " sources");
    if (sources == 0) {
      p.targetSrc().delete();
    }

    int includes = 0;
    includes += copyDirectory(p.include(), p.targetInclude(), SOURCE_FILTER);
    log.debug("copied " + includes + " includes");
    if (includes == 0) {
      p.targetInclude().delete();
    }

    int resources = 0;
    resources += copyDirectory(p.priv(), p.targetPriv(), NULL_FILTER);
    resources += copyDirectory(p.resources(), p.targetPriv(), NULL_FILTER);
    log.debug("copied " + resources + " resources");
    if (resources == 0) {
      p.targetPriv().delete();
    }

    int testResources = 0;
    testResources += copyDirectory(p.test_resources(), p.targetTest(), NULL_FILTER);
    log.debug("copied " + testResources + " test resources");
    if (testResources == 0) {
      p.targetTest().delete();
    }

    final FileFilter filter;
    if (p.packagingType() == PackagingType.ERLANG_OTP) {
      filter = otpFilter;
    }
    else {
      filter = stdFilter;
    }
    for (File source : getFilesAndDirectoriesRecursive(p.src_base(), filter)) {
      int other = 0;
      String destName = source.getName().replace(SRC_SUFFIX, "") + SRC_SUFFIX;
      File destination = new File(p.targetProject(), destName);
      other += copyDirectory(source, destination, NULL_FILTER);
      log.debug("copied " + other + " non-erlang sources");
      if (other == 0) {
        destination.delete();
      }
    }
  }
}
