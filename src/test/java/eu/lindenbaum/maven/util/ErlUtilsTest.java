package eu.lindenbaum.maven.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

public class ErlUtilsTest {
  private static final Log log = new SystemStreamLog();

  private static final String DIR_COMMAND = "dir";
  private static final String LS_COMMAND = "ls";

  @Test
  public void testToPlainListNull() {
    String result = ErlUtils.toPlainList((String[]) null, null);
    assertEquals("[]", result);
  }

  @Test
  public void testToPlainListNonNull() {
    Integer[] list = new Integer[]{ 1, 2, 3, 4, 5 };
    String result = ErlUtils.toPlainList(list, new Predicate<Integer>() {
      @Override
      public boolean pred(Integer number) {
        return number % 2 == 0;
      }
    });
    assertEquals("[2, 4]", result);
  }

  @Test
  public void testToStringListNull() {
    String result = ErlUtils.toStringList((String[]) null, null);
    assertEquals("[]", result);
  }

  @Test
  public void testToStringListNonNull() {
    Integer[] list = new Integer[]{ 1, 2, 3, 4, 5 };
    String result = ErlUtils.toStringList(list, new Predicate<Integer>() {
      @Override
      public boolean pred(Integer number) {
        return number % 2 == 0;
      }
    });
    assertEquals("[\"2\", \"4\"]", result);
  }

  @Test
  public void testExec() throws Exception {
    String dir = getSystemSpecificListCommand();
    List<String> command = Arrays.asList(new String[]{ dir });
    assertEquals("ok", ErlUtils.exec(command, log, null, new Observer() {
      @Override
      public String handle(int exitValue, String result) {
        assertEquals(exitValue, 0);
        assertNotNull(result);
        return "ok";
      }
    }));
  }

  private String getSystemSpecificListCommand() {
    if ("Mac OS X".equals(System.getProperty("os.name"))) {
      return LS_COMMAND;
    }
    else {
      return DIR_COMMAND;
    }
  }

  @Test
  public void testEval2() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")"));
  }

  @Test
  public void testEval3() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")", null));
  }

  @Test
  public void testEval4() throws Exception {
    assertEquals("ok", ErlUtils.eval(log, "io:format(\"ok~n\")", null, null));
  }
}
