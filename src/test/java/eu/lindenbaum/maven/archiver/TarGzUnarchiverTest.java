package eu.lindenbaum.maven.archiver;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import eu.lindenbaum.maven.Util;

import org.junit.BeforeClass;
import org.junit.Test;

public class TarGzUnarchiverTest {
  @BeforeClass
  public static void setupBeforeClass() {
    Util.startBackendNode();
  }

  @Test
  public void testArchiveDoesNotExist() {
    TarGzUnarchiver unarchiver = new TarGzUnarchiver(Util.PEER);
    try {
      unarchiver.extract(new File("not-existing.tar.gz"));
      fail("IOException expected");
    }
    catch (IOException expected) {
      // OK
    }
  }

  @Test
  public void testSuccess() throws Exception {
    URL resource = getClass().getClassLoader().getResource("tar-gz-unarchiver");
    File destination = new File(resource.getFile());
    TarGzUnarchiver unarchiver = new TarGzUnarchiver(Util.PEER, destination);

    File archive = new File(destination, "test-archive.tar.gz");
    assertTrue(archive.isFile());
    unarchiver.extract(archive);

    File extracted1 = new File(destination, "tar-gz-archived");
    extracted1.deleteOnExit();
    assertTrue(extracted1.isDirectory());

    File extracted2 = new File(extracted1, "folder1");
    extracted1.deleteOnExit();
    assertTrue(extracted2.isDirectory());

    File extracted3 = new File(extracted2, "file1.txt");
    extracted2.deleteOnExit();
    assertTrue(extracted3.isFile());
  }
}
