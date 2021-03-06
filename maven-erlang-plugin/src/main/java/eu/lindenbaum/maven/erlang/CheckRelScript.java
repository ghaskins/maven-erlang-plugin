package eu.lindenbaum.maven.erlang;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import eu.lindenbaum.maven.util.ErlUtils;

/**
 * A {@link Script} that can be used to extract certain values from an erlang
 * release file.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public class CheckRelScript implements Script<CheckRelResult> {
  private static final String script = //
  NL + "case file:consult(\"%s\") of" + NL + //
      "    {ok, [{release, {N, V}, {erts, E}, Apps}]} ->" + NL + //
      "        {N, V, E, lists:map(" + NL + //
      "                    fun({App, Vsn}) -> {App, Vsn};" + NL + //
      "                       ({App, Vsn, _}) -> {App, Vsn};" + NL + //
      "                       ({App, Vsn, _, _}) -> {App, Vsn}" + NL + //
      "                    end, Apps)};" + NL + //
      "    _ ->" + NL + //
      "        {undefined, undefined, undefined, []} " + NL + // do not remove the trailing whitespace!
      "end." + NL;

  private final File relFile;

  /**
   * Creates an extraction {@link Script} for a specific release file.
   * 
   * @param relFile to extract values from
   * @see http://www.erlang.org/doc/man/rel.html
   */
  public CheckRelScript(File relFile) {
    this.relFile = relFile;
  }

  @Override
  public String get() {
    return String.format(script, this.relFile.getAbsolutePath());
  }

  /**
   * Converts the result of the {@link Script} execution into a
   * {@link CheckRelResult} bean holding interesting values from the release
   * file.
   * 
   * @param result The return term of the {@link Script} execution.
   * @return An object capable of delivering the results transparently.
   */
  @Override
  public CheckRelResult handle(OtpErlangObject result) {
    OtpErlangTuple resultTuple = (OtpErlangTuple) result;
    final OtpErlangObject releaseName = resultTuple.elementAt(0);
    final OtpErlangObject releaseVersion = resultTuple.elementAt(1);
    final OtpErlangObject ertsVersion = resultTuple.elementAt(2);
    final OtpErlangList applications = (OtpErlangList) resultTuple.elementAt(3);
    return new CheckRelResult() {
      @Override
      public String getName() {
        return ErlUtils.toString(releaseName);
      }

      @Override
      public String getReleaseVersion() {
        return ErlUtils.toString(releaseVersion);
      }

      @Override
      public String getErtsVersion() {
        return ErlUtils.toString(ertsVersion);
      }

      @Override
      public Map<String, String> getApplications() {
        Map<String, String> resultMap = new HashMap<String, String>();
        for (int i = 0; i < applications.arity(); ++i) {
          OtpErlangTuple appTuple = (OtpErlangTuple) applications.elementAt(i);
          String application = ErlUtils.toString(appTuple.elementAt(0));
          String version = ErlUtils.toString(appTuple.elementAt(1));
          resultMap.put(application, version);
        }
        return resultMap;
      }
    };
  }
}
