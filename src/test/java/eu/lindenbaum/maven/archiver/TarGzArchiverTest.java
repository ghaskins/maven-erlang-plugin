package eu.lindenbaum.maven.archiver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import eu.lindenbaum.maven.Util;

import org.junit.BeforeClass;
import org.junit.Test;

public class TarGzArchiverTest {
  @BeforeClass
  public static void setupBeforeClass() {
    Util.startBackendNode();
  }

  @Test
  public void testNoFiles() {
    URL resource = getClass().getClassLoader().getResource("tar-gz-unarchiver");
    File testRoot = new File(resource.getFile());
    File archive = new File(testRoot, "archive.tar.gz");
    archive.delete();

    TarGzArchiver archiver = new TarGzArchiver(Util.PEER, archive);
    try {
      archiver.createArchive();
      fail("IOException expected");
    }
    catch (IOException expected) {
      // OK
    }

    assertFalse(archive.exists());
  }

  @Test
  public void testSuccess() throws Exception {
    URL resource = getClass().getClassLoader().getResource("tar-gz-unarchiver");
    File testRoot = new File(resource.getFile());
    File archive = new File(testRoot, "archive.tar.gz");
    assertFalse(archive.exists());
    archive.deleteOnExit();

    TarGzArchiver archiver = new TarGzArchiver(Util.PEER, archive);
    File toBeArchived = new File(resource.getFile());
    archiver.addFile(toBeArchived, "tar-gz-archived");
    archiver.createArchive();

    assertTrue(archive.isFile());
  }
}
