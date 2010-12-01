package eu.lindenbaum.maven.erlang;

import java.io.File;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangString;
import com.ericsson.otp.erlang.OtpErlangTuple;

import eu.lindenbaum.maven.util.ErlUtils;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link Script} executing a list of (eunit) tests.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class TestScript implements Script<TestResult> {
  private static final String script = //
  "    Surefire = {report, {surefire, [{dir, \"%s\"}, {package, \"%s\"}]}}," + //
      "Tty = {report, {ttycapture, [{report_to, self()}]}}," + //
      "case eunit:test(%s, [Surefire, Tty]) of" + //
      "    ok ->" + //
      "        receive" + //
      "            Result ->" + //
      "                Result" + //
      "        end;" + //
      "    {error, Reason} ->" + //
      "        {error, [lists:flatten(io_lib:format(\"~p\", [Reason]))]} " + //
      "end.";

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
    final OtpErlangList lines = (OtpErlangList) resultTuple.elementAt(1);
    return new TestResult() {
      @Override
      public boolean testsPassed() {
        return !"error".equals(level.atomValue());
      }

      @Override
      public void logOutput(Log log) {
        for (int i = 0; i < lines.arity(); ++i) {
          final String line;
          if (lines.elementAt(i) instanceof OtpErlangString) {
            line = ((OtpErlangString) lines.elementAt(i)).stringValue().trim();
          }
          else {
            line = "";
          }
          if ("info".equals(level.atomValue())) {
            log.info(line);
          }
          else if ("warn".equals(level.atomValue())) {
            log.warn(line);
          }
          else {
            log.error(line);
          }
        }
      }
    };
  }
}
