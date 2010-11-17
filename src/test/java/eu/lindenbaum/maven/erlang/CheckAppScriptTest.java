package eu.lindenbaum.maven.erlang;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import org.junit.Test;

public class CheckAppScriptTest {
  @Test
  public void testGet() {
    File appFile = new File("appFile");

    CheckAppScript script = new CheckAppScript(appFile);
    String expression = script.get();
    assertNotNull(expression);
    assertFalse(expression.isEmpty());
    assertFalse(expression.contains("%s"));
  }

  @Test
  public void testHandleDefault() {
    File appFile = new File("appFile");

    OtpErlangAtom undef = new OtpErlangAtom("undefined");
    OtpErlangList nil = new OtpErlangList();
    OtpErlangTuple result = new OtpErlangTuple(new OtpErlangObject[]{ undef, undef, undef, nil, nil });

    CheckAppScript script = new CheckAppScript(appFile);
    CheckAppResult appResult = script.handle(result);
    assertEquals("undefined", appResult.getName());
    assertEquals("undefined", appResult.getVersion());
    assertEquals("undefined", appResult.getStartModule());
    assertArrayEquals(new String[0], appResult.getModules());
    assertArrayEquals(new String[0], appResult.getApplications());
  }

  @Test
  public void testHandle() {
    File appFile = new File("appFile");

    OtpErlangAtom name = new OtpErlangAtom("name");
    OtpErlangString version = new OtpErlangString("1.0.0-SNAPSHOT");
    OtpErlangAtom startModule = new OtpErlangAtom("startModule");
    OtpErlangList modules = new OtpErlangList(new OtpErlangObject[]{ new OtpErlangAtom("module1"),
                                                                    new OtpErlangAtom("Module2") });
    OtpErlangList applications = new OtpErlangList(new OtpErlangObject[]{ new OtpErlangAtom("sasl") });
    OtpErlangTuple result = new OtpErlangTuple(new OtpErlangObject[]{ name, version, startModule, modules,
                                                                     applications });

    CheckAppScript script = new CheckAppScript(appFile);
    CheckAppResult appResult = script.handle(result);
    assertEquals("name", appResult.getName());
    assertEquals("1.0.0-SNAPSHOT", appResult.getVersion());
    assertEquals("startModule", appResult.getStartModule());
    assertArrayEquals(new String[]{ "module1", "Module2" }, appResult.getModules());
    assertArrayEquals(new String[]{ "sasl" }, appResult.getApplications());
  }
}
