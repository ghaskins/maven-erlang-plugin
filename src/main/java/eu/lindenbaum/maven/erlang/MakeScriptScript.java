package eu.lindenbaum.maven.erlang;

import java.io.File;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import eu.lindenbaum.maven.util.ErlUtils;

import org.apache.maven.plugin.logging.Log;

/**
 * A {@link Script} generating release scripts using
 * <code>systools:make_script/2</code>
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Olle Törnström <olle.toernstroem@lindenbaum.eu>
 */
public final class MakeScriptScript implements Script<MakeScriptResult> {
  private static final String script = //
  "    case systools:make_script(\"%s\", [silent, {outdir, \"%s\"}] ++ [%s]) of" + //
      "    ok -> {ok, \"\"};" + //
      "    error -> {error, \"unknown\"};" + //
      "    {ok, Module, Warnings} ->" + //
      "        {warn, Module:format_warning(Warnings)};" + //
      "    {error, Module, Error} ->" + //
      "        {error, Module:format_error(Error)}" + //
      "end.";

  private final String releaseName;
  private final File outdir;
  private final String options;

  public MakeScriptScript(String releaseName, File outdir, String options) {
    this.releaseName = releaseName;
    this.outdir = outdir;
    this.options = options != null ? options : "";
  }

  @Override
  public String get() {
    String outPath = this.outdir.getAbsolutePath();
    return String.format(script, this.releaseName, outPath, this.options);
  }

  /**
   * Converts the result of the {@link Script} execution into an object capable
   * of logging the errors/warnings as well as returning the script result.
   * 
   * @param result The return term of the {@link Script} execution.
   * @return An object capable of delivering the results transparently.
   */
  @Override
  public MakeScriptResult handle(OtpErlangObject result) {
    OtpErlangTuple resultTuple = (OtpErlangTuple) result;
    final String level = ErlUtils.cast(resultTuple.elementAt(0));
    final String messages = ErlUtils.cast(resultTuple.elementAt(1));
    return new MakeScriptResult() {
      @Override
      public boolean success() {
        return "ok".equals(level) || "warn".equals(level);
      }

      @Override
      public void logOutput(Log log) {
        String[] lines = messages.split("\r?\n");
        for (String line : lines) {
          if ("error".equals(level)) {
            log.error(line);
          }
          else if ("warn".equals(level)) {
            log.warn(line);
          }
        }
      }
    };
  }
}
