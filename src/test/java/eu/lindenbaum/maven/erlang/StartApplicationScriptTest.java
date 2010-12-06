package eu.lindenbaum.maven.erlang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import org.junit.Test;

public class StartApplicationScriptTest {
  @Test
  public void testGetHandle() {
    List<File> codePaths = Arrays.asList(new File("codePath"));
    List<File> modules = Arrays.asList(new File("module"));
    List<String> applications = Arrays.asList("application");
    StartApplicationScript script = new StartApplicationScript(codePaths, modules, applications);
    String expression = script.get();
    assertNotNull(expression);
    assertFalse(expression.isEmpty());
    assertFalse(expression.contains("%s"));
  }

  @Test
  public void testHandleSuccess() {
    OtpErlangAtom success = new OtpErlangAtom("ok");
    OtpErlangAtom beforeApp = new OtpErlangAtom("beforeApp");
    OtpErlangList beforeApps = new OtpErlangList(new OtpErlangObject[]{ beforeApp });
    OtpErlangTuple result = new OtpErlangTuple(new OtpErlangObject[]{ success, beforeApps });

    List<File> codePaths = Arrays.asList(new File("codePath"));
    List<File> modules = Arrays.asList(new File("module"));
    List<String> applications = Arrays.asList("application");
    StartApplicationScript script = new StartApplicationScript(codePaths, modules, applications);
    StartResult startResult = script.handle(result);
    assertTrue(startResult.startSucceeded());
    List<String> apps = startResult.getBeforeApplications();
    assertEquals(1, apps.size());
    assertEquals("beforeApp", apps.get(0));
  }

  @Test
  public void testHandleFailure() {
    OtpErlangAtom success = new OtpErlangAtom("error");
    OtpErlangAtom what = new OtpErlangAtom("what");
    OtpErlangTuple error = new OtpErlangTuple(new OtpErlangObject[]{ success, what });
    OtpErlangAtom beforeApp = new OtpErlangAtom("beforeApp");
    OtpErlangList beforeApps = new OtpErlangList(new OtpErlangObject[]{ beforeApp });
    OtpErlangTuple result = new OtpErlangTuple(new OtpErlangObject[]{ error, beforeApps });

    List<File> codePaths = Arrays.asList(new File("codePath"));
    List<File> modules = Arrays.asList(new File("module"));
    List<String> applications = Arrays.asList("application");
    StartApplicationScript script = new StartApplicationScript(codePaths, modules, applications);
    StartResult startResult = script.handle(result);
    assertFalse(startResult.startSucceeded());
    List<String> apps = startResult.getBeforeApplications();
    assertEquals(1, apps.size());
    assertEquals("beforeApp", apps.get(0));
  }
}
