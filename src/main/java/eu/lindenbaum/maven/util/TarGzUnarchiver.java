package eu.lindenbaum.maven.util;

import java.io.File;
import java.io.IOException;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpPeer;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * An unarchiver that can extract gzipped tar archives using the erlang
 * {@code erl_tar} module. This archiver will overwrite older files by default.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class TarGzUnarchiver {
  private static final String script = "erl_tar:extract(\"%s\", [compressed, {cwd, \"%s\"}]).";

  private final OtpPeer peer;
  private final File destination;

  public TarGzUnarchiver(OtpPeer peer) {
    this(peer, new File("."));
  }

  public TarGzUnarchiver(OtpPeer peer, File destination) {
    this.peer = peer;
    this.destination = destination;
  }

  /**
   * Returns this unarchivers destination directory. No matter if existing or
   * not.
   * 
   * @return this unarchivers destination directory
   */
  public File getDestination() {
    return this.destination;
  }

  /**
   * Extracts a given archive into the configured directory.
   * 
   * @param archive to extract
   * @throws IOException in case the archive does not exist or the destination
   *           directory cannot be created or is not a directory or
   *           {@code erl_tar} ends with errors.
   */
  public void extract(File archive) throws IOException {
    String archivePath = archive.getAbsolutePath();
    String destinationPath = this.destination.getPath();
    if (archive.isFile()) {
      this.destination.mkdirs();
      if (this.destination.exists()) {
        if (this.destination.isDirectory()) {
          String expression = String.format(script, archivePath, destinationPath);
          try {
            MavenSelf self = MavenSelf.get();
            OtpErlangObject result = self.eval(this.peer, expression);
            if (!"ok".equals(result.toString())) {
              throw new IOException("failed to extract archive: " + result);
            }
          }
          catch (MojoExecutionException e) {
            throw new IOException(e.getMessage(), e);
          }
        }
        else {
          throw new IOException(destinationPath + " is not a directory");
        }
      }
      else {
        throw new IOException("could not create " + destinationPath);
      }
    }
    else {
      throw new IOException(archivePath + " does not exist");
    }
  }
}
