package eu.lindenbaum.maven.archiver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpPeer;

import eu.lindenbaum.maven.util.MavenSelf;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * An archiver that can create gzipped tar archives using the erlang
 * {@code erl_tar} module.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class TarGzArchiver {
  private static final String script = "erl_tar:create(\"%s\", %s, [compressed]).";

  private final OtpPeer peer;
  private final File archive;
  private final Map<File, String> files = new HashMap<File, String>();

  public TarGzArchiver(OtpPeer peer, File archive) {
    this.peer = peer;
    this.archive = archive;
  }

  /**
   * Returns this archivers archive file no matter already created or not.
   * 
   * @return this archivers archive file
   */
  public File getArchive() {
    return this.archive;
  }

  /**
   * Add a file/directory to the archive.
   * 
   * @param file to add to the archive
   * @throws IOException if the file does not exist
   */
  public void addFile(File file) throws IOException {
    addFile(file, file.getName());
  }

  /**
   * Add a file/directory to the archive.
   * 
   * @param file to add to the archive
   * @param archiveName name of the file in the archive
   * @throws IOException if the file does not exist
   */
  public void addFile(File file, String archiveName) throws IOException {
    if (file.exists()) {
      this.files.put(file, archiveName);
    }
    else {
      throw new IOException("cannot add non-existing file " + file);
    }
  }

  /**
   * Creates and writes the archive to {@link #archive}.
   * 
   * @throws IOException if there are no files to archive or {@code erl_tar}
   *           ends with erros
   */
  public void createArchive() throws IOException {
    if (this.files.isEmpty()) {
      throw new IOException("no files to package");
    }
    else {
      String archivePath = this.archive.getAbsolutePath();
      String expression = String.format(script, archivePath, getFiles(this.files));
      try {
        OtpErlangObject result = MavenSelf.get().eval(this.peer, expression);
        if (!"ok".equals(result.toString())) {
          throw new IOException("failed to create archive: " + result);
        }
      }
      catch (MojoExecutionException e) {
        throw new IOException(e.getMessage(), e);
      }
    }
  }

  /**
   * Returns a list of the files to archive. It is assumed that there's more
   * than zero files to archive.
   * 
   * @param files mapping of files to package
   * @return a string containing an erlang list of file mappings
   */
  private static String getFiles(Map<File, String> files) {
    StringBuilder fileList = new StringBuilder("[");
    for (Entry<File, String> file : files.entrySet()) {
      fileList.append("{\"");
      fileList.append(file.getValue());
      fileList.append("\",\"");
      fileList.append(file.getKey().getAbsolutePath());
      fileList.append("\"},");
    }
    fileList.deleteCharAt(fileList.length() - 1);
    fileList.append("]");
    return fileList.toString();
  }
}
