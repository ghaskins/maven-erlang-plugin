package eu.lindenbaum.maven.erlang;

import java.io.File;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import eu.lindenbaum.maven.util.ErlUtils;
import eu.lindenbaum.maven.util.MavenUtils;
import eu.lindenbaum.maven.util.MavenUtils.LogLevel;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link Script} executing a list of (eunit) tests.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class TestScript implements Script<TestResult> {
  private static final String script = //
  NL + "Surefire = {report, {surefire, [{dir, \"%s\"}, {package, \"%s.\"}]}}," + NL + //
      "Tty = {report, {ttycapture, [{report_to, self()}]}}," + NL + //
      "Out = try eunit:test(%s, [Surefire, Tty]) of" + NL + //
      "          _ -> []" + NL + //
      "      catch" + NL + //
      "          Class:Exception ->" + NL + //
      "              Msg = io_lib:format(\"~p:~p\", [Class, Exception])," + NL + //
      "              [lists:flatten(Msg)]" + NL + //
      "      end," + NL + //
      "receive {Level, Captured} -> {Level, Captured ++ Out} end." + NL;

  private final List<File> tests;
  private final File surefireDir;
  private final String suiteName;

  /**
   * Creates a {@link Script} executing a list of eunit tests.
   * 
   * @param tests to run
   * @param surefireDir to output surefire compatible reports into
   * @param suiteName the name of the test suite (for surefire)
   */
  public TestScript(List<File> tests, File surefireDir, String suiteName) {
    this.tests = tests;
    this.surefireDir = surefireDir;
    this.suiteName = suiteName;
  }

  @Override
  public String get() {
    String surefirePath = this.surefireDir.getAbsolutePath();
    String testList = ErlUtils.toModuleList(this.tests, "'", "'");
    return String.format(script, surefirePath, this.suiteName, testList);
  }

  /**
   * Converts the result of the {@link Script} execution into an object capable
   * of logging the test output as well as returning whether the unit test
   * execution succeeded.
   * 
   * @param result The return term of the {@link Script} execution.
   * @return An object capable of delivering the results transparently.
   */
  @Override
  public TestResult handle(OtpErlangObject result) {
    OtpErlangTuple resultTuple = (OtpErlangTuple) result;
    final OtpErlangAtom level = (OtpErlangAtom) resultTuple.elementAt(0);
    final OtpErlangList output = (OtpErlangList) resultTuple.elementAt(1);
    return new TestResult() {
      @Override
      public boolean testsPassed() {
        return !"error".equals(level.atomValue());
      }

      @Override
      public void logOutput(Log log) {
        LogLevel logLevel = LogLevel.fromString(level.atomValue());
        for (int i = 0; i < output.arity(); ++i) {
          MavenUtils.logMultiLineString(log, logLevel, ErlUtils.toString(output.elementAt(i)));
        }
      }
    };
  }
}
