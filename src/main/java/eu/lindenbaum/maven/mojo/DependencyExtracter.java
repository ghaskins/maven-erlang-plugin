package eu.lindenbaum.maven.mojo;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import eu.lindenbaum.maven.archiver.TarGzUnarchiver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Unpack {@code erlang-otp} or {@code erlang-std} dependencies. This will
 * unpack all dependencies of this {@link MavenProject} into the
 * {@code target/lib} directory. This is done only in case the dependency has
 * changed since the last unpack process.
 * 
 * @goal extract-dependencies
 * @phase generate-sources
 * @requiresDependencyResolution test
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class DependencyExtracter extends ErlangMojo {
  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    File targetLib = p.targetLib();
    targetLib.mkdirs();
    TarGzUnarchiver unarchiver = new TarGzUnarchiver(p.node(), targetLib);
    @SuppressWarnings("unchecked")
    Set<Artifact> artifacts = p.project().getArtifacts();
    log.debug("found artifacts " + artifacts);
    for (Artifact artifact : artifacts) {
      String type = artifact.getType();
      if (PackagingType.ERLANG_OTP.isA(type) || PackagingType.ERLANG_STD.isA(type)) {
        extractArtifact(log, artifact, unarchiver);
      }
    }
  }

  /**
   * Extract a specific artifact (.zip file) into a specific directory.
   * 
   * @param log logger to use
   * @param artifact to extract
   * @param unarchiver directory to extract the artifact into
   * @throws MojoExecutionException
   */
  private static void extractArtifact(Log log, Artifact artifact, TarGzUnarchiver unarchiver) throws MojoExecutionException {
    File artifactFile = artifact.getFile();
    String artifactdirectory = getArtifactDirectory(artifact);
    File cachedDependency = new File(unarchiver.getDestination(), artifactdirectory);
    if (!cachedDependency.isDirectory() || artifactFile.lastModified() > cachedDependency.lastModified()) {
      log.info("Extracting artifact " + artifact.getGroupId() + ":" + artifact.getId());
      try {
        unarchiver.extract(artifact.getFile());
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
    }
    else {
      log.debug("Skipping artifact " + artifact.getGroupId() + ":" + artifact.getId());
    }
  }

  /**
   * Returns the directory name for the given {@link Artifact}.
   * 
   * @param artifact to retrieve the directory name from
   * @return a string containing the directory name
   */
  private static String getArtifactDirectory(Artifact artifact) {
    return artifact.getFile().getName().replace("." + artifact.getType(), "");
  }
}
