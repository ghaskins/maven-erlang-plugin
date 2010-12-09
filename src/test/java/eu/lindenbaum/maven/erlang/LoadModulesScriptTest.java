package eu.lindenbaum.maven.erlang;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;

import org.junit.Test;

public class LoadModulesScriptTest {
  @Test
  public void testGet() {
    List<File> modules = Arrays.asList(new File("module"));
    List<File> codePaths = Arrays.asList(new File("path"));
    LoadModulesScript script = new LoadModulesScript(modules, codePaths);
    String expression = script.get();
    assertNotNull(expression);
    assertFalse(expression.isEmpty());
    assertFalse(expression.contains("%s"));
  }

  @Test
  public void testHandle() {
    OtpErlangAtom ignored = new OtpErlangAtom("ignored");

    List<File> modules = Arrays.asList(new File("module"));
    List<File> codePaths = Arrays.asList(new File("path"));
    LoadModulesScript script = new LoadModulesScript(modules, codePaths);
    assertNull(script.handle(ignored));
  }
}
