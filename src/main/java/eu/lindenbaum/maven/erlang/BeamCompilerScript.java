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
 * A {@link Script} that can be used to compile erlang files. In case of
 * compiler errors the script will leave the not yet compiled files uncompiled.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class BeamCompilerScript implements Script<CompilerResult> {
  private static final String script = //
  "    Options = [return, {outdir, \"%s\"}] ++ %s ++ %s," + //
      "lists:foldl(" + //
      "  fun(ToCompile, {\"\", Reports}) ->" + //
      "          {Fail, Messages} = case compile:file(ToCompile, Options) of" + //
      "                               {error, E, W} ->" + //
      "                                   {ToCompile," + //
      "                                    lists:map(fun(Elem) -> {error, Elem} end, E)" + //
      "                                    ++ lists:map(fun(Elem) -> {warn, Elem} end, W)};" + //
      "                               {ok, _, W} ->" + //
      "                                   {\"\"," + //
      "                                    lists:map(fun(Elem) -> {warn, Elem} end, W)}" + //
      "                             end," + //
      "          R = lists:foldr(" + //
      "                fun({Level, {File, Exceptions}}, Acc) ->" + //
      "                        lists:map(fun({Line, Module, Info}) ->" + //
      "                                          Formatted = Module:format_error(Info)," + //
      "                                          Flattened = lists:flatten(Formatted)," + //
      "                                          S = io_lib:format(\"~s:~p: ~s\", [File, Line, Flattened])," + //
      "                                          {Level, lists:flatten(S)};" + //
      "                                     (Else) ->" + //
      "                                          S = io_lib:format(\"~s: ~p\", [File, Else])," + //
      "                                          {Level, lists:flatten(S)}" + //
      "                                  end, Exceptions) ++ Acc" + //
      "                end, [], Messages)," + //
      "          {Fail, Reports ++ R};" + //
      "     (_, Result) ->" + //
      "          Result" + //
      "  end, {\"\", []}, %s).";

  private final List<File> files;
  private final File outdir;
  private final List<File> includes;
  private final List<String> options;

  /**
   * Creates a compiler script for a {@link List} of erlang files.
   * 
   * @param files a list of files to compile
   * @param outdir the destination directory for the compiled .beam
   * @param includes a list of include directories
   * @param options a list of compiler options according to the erlang docs
   * @see http://www.erlang.org/doc/man/compile.html
   */
  public BeamCompilerScript(List<File> files, File outdir, List<File> includes, List<String> options) {
    this.files = files;
    this.outdir = outdir;
    this.includes = includes;
    this.options = options;
  }

  @Override
  public String get() {
    String out = this.outdir.getAbsolutePath();
    String incs = ErlUtils.toFileList(this.includes, "{i, \"", "\"}");
    String opts = ErlUtils.toList(this.options, null, "", "");
    String files = ErlUtils.toFileList(this.files, "\"", "\"");
    return String.format(script, out, incs, opts, files);
  }

  /**
   * Converts the result of the {@link Script} execution into an object logging
   * the compiler output correctly as well as providing failed compilation
   * units, if any.
   * 
   * @param result The return term of the {@link Script} execution.
   * @return An object capable of delivering the results transparently.
   */
  @Override
  public CompilerResult handle(OtpErlangObject result) {
    OtpErlangTuple r = (OtpErlangTuple) result;
    final OtpErlangObject failed = r.elementAt(0);
    final OtpErlangList messages = (OtpErlangList) r.elementAt(1);
    return new CompilerResult() {
      @Override
      public void logOutput(Log log) {
        for (int i = 0; i < messages.arity(); ++i) {
          OtpErlangTuple messageTuple = (OtpErlangTuple) messages.elementAt(i);
          OtpErlangAtom level = (OtpErlangAtom) messageTuple.elementAt(0);
          OtpErlangString message = (OtpErlangString) messageTuple.elementAt(1);
          if ("error".equals(level.atomValue())) {
            log.error(message.stringValue().trim());
          }
          else {
            log.warn(message.stringValue().trim());
          }
        }
      }

      @Override
      public String getFailed() {
        if (failed instanceof OtpErlangString) {
          OtpErlangString filename = (OtpErlangString) failed;
          String converted = filename.stringValue().trim();
          return converted.isEmpty() ? null : converted;
        }
        return null;
      }
    };
  }
}
