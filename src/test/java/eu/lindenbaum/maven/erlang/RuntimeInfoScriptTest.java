package eu.lindenbaum.maven.erlang;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import com.ericsson.otp.erlang.OtpErlangString;

import org.junit.Test;

public class RuntimeInfoScriptTest {
  @Test
  public void testGet() {
    RuntimeInfoScript script = new RuntimeInfoScript();
    String expression = script.get();
    assertNotNull(expression);
    assertFalse(expression.isEmpty());
    assertFalse(expression.contains("%s"));
  }

  @Test
  public void testHandle() {
    OtpErlangString result = new OtpErlangString("/path/");

    RuntimeInfoScript script = new RuntimeInfoScript();
    RuntimeInfo info = script.handle(result);
    assertNotNull(info);
    File libDirectory = info.libDirectory();
    assertNotNull(libDirectory);
  }
}
