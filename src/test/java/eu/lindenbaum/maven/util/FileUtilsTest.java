package eu.lindenbaum.maven.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.List;

import org.junit.Test;

public class FileUtilsTest {
  @Test
  public void testGetFilesRecursive() {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File root = new File(resource.getFile());
    List<File> files = FileUtils.getFilesRecursive(root, ".txt");
    assertEquals(2, files.size());
    assertEquals("file1.txt", files.get(0).getName());
    assertEquals("file0.txt", files.get(1).getName());
  }

  @Test
  public void testGetDirectoriesRecursive() {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File root = new File(resource.getFile());
    List<File> files = FileUtils.getDirectoriesRecursive(root, new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return "ebin".equals(pathname.getName());
      }
    });
    assertEquals(3, files.size());
  }

  @Test
  public void testGetFilesAndDirectoriesRecursive() {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File root = new File(resource.getFile());
    List<File> files = FileUtils.getFilesAndDirectoriesRecursive(root, FileUtils.NULL_FILTER);
    assertEquals(12, files.size());
  }

  @Test
  public void testCopyDirectory() throws Exception {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File fileUtils = new File(resource.getFile());
    File root = new File(fileUtils, "app-1.0");
    File to = new File("target");
    int copied = FileUtils.copyDirectory(root, to, FileUtils.NULL_FILTER);
    File ebin = new File(to, "ebin");
    File file = new File(ebin, "file");
    boolean parent = ebin.isDirectory();
    boolean child = file.isFile();
    file.delete();
    ebin.delete();
    assertEquals(1, copied);
    assertTrue(parent);
    assertTrue(child);
  }

  @Test
  public void testCopyDirectoryEmpty() throws Exception {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File fileUtils = new File(resource.getFile());
    File root = new File(fileUtils, "subdirectory3");
    File to = new File("target");
    assertEquals(0, FileUtils.copyDirectory(root, to, FileUtils.NULL_FILTER));
    assertFalse(new File(to, "subdirectory3").isDirectory());
  }

  @Test
  public void testOtpDirectoryRegex() {
    assertTrue(FileUtils.EBIN_PATTERN.matcher("test-path/app-1.0/ebin").matches());
    assertTrue(FileUtils.EBIN_PATTERN.matcher("path/app-1.0/ebin").matches());
    assertFalse(FileUtils.EBIN_PATTERN.matcher("path/app/ebin").matches());
    assertFalse(FileUtils.EBIN_PATTERN.matcher("test-path/app/ebin").matches());
  }

  @Test
  public void testGetDependencies() {
    URL resource = getClass().getClassLoader().getResource("file-utils");
    File root = new File(resource.getFile());
    List<File> files = FileUtils.getDependencies(root);
    assertEquals(1, files.size());
  }
}
