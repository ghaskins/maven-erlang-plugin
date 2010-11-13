package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.erlang.MavenSelf.DEFAULT_PEER;
import static eu.lindenbaum.maven.util.ErlConstants.ARTIFACT_TYPE_OTP;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import eu.lindenbaum.maven.archiver.TarGzUnarchiver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Unpack {@code erlang-otp} dependencies. This will unpack all dependencies of
 * this {@link MavenProject} into the {@link AbstractErlangMojo#targetLib}
 * directory. This is done only in case the dependency has changed since the
 * last unpack process.
 * 
 * @goal unpack-dependencies
 * @phase generate-sources
 * @requiresDependencyResolution test
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class UnpackDependenciesMojo extends AbstractErlangMojo {
  @Override
  @SuppressWarnings("unchecked")
  public void execute() throws MojoExecutionException {
    Log log = getLog();
    this.targetLib.mkdirs();
    TarGzUnarchiver unarchiver = new TarGzUnarchiver(DEFAULT_PEER, this.targetLib);
    Set<Artifact> artifacts = this.project.getArtifacts();
    log.debug("found artifacts " + artifacts);
    for (Artifact artifact : artifacts) {
      if (artifact.getType().equals(ARTIFACT_TYPE_OTP)) {
        extractArtifact(artifact, unarchiver);
      }
    }
  }

  /**
   * Extract a specific artifact (.zip file) into a specific directory.
   * 
   * @param artifact to extract
   * @param unarchiver directory to extract the artifact into
   * @throws MojoExecutionException
   */
  private void extractArtifact(Artifact artifact, TarGzUnarchiver unarchiver) throws MojoExecutionException {
    Log log = getLog();
    File artifactFile = artifact.getFile();
    String artifactdirectory = getArtifactDirectory(artifact);
    File cachedDependency = new File(unarchiver.getDestination(), artifactdirectory);
    if (!cachedDependency.isDirectory() || artifactFile.lastModified() > cachedDependency.lastModified()) {
      log.debug("extracting artifact " + artifact.getGroupId() + ":" + artifact.getId());
      try {
        unarchiver.extract(artifact.getFile());
      }
      catch (IOException e) {
        throw new MojoExecutionException(e.getMessage(), e);
      }
    }
    else {
      log.debug("skipping artifact " + artifact.getGroupId() + ":" + artifact.getId());
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
