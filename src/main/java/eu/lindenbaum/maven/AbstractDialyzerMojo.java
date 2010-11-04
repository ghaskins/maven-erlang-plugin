package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.util.ErlUtils.toPlainList;
import static eu.lindenbaum.maven.util.ErlUtils.toStringList;
import static eu.lindenbaum.maven.util.FileUtils.FILE_PRED;
import static eu.lindenbaum.maven.util.MavenSelf.DEFAULT_PEER;

import java.io.File;
import java.util.List;

import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

import eu.lindenbaum.maven.util.MavenSelf;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Base class for {@link Mojo}s using the {@code dialyzer tool}.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @author Timo Koepke <timo.koepke@lindenbaum.eu>
 */
abstract class AbstractDialyzerMojo extends AbstractErlangMojo {
  private static final String script = //
  "lists:map(fun({_, {File, Line}, Msg}) ->" + //
      "         {File, Line, Msg}" + //
      "      end, dialyzer:run([{from, src_code}," + //
      "                         {files_rec, %s}," + //
      "                         {include_dirs, %s}," + //
      "                         {get_warnings, true}," + //
      "                         {warnings, %s}])).";

  /**
   * Setting this to {@code true} will break the build when a {@code dialyzer}
   * run returns warnings.
   * 
   * @parameter default-value=false
   */
  boolean dialyzerWarningsAreErrors;

  /**
   * Additional {@code dialyzer} warning options. These must be valid warning
   * atoms that will be included when calling
   * <code>dialyzer:run([{warnings,[...]}, ...])</code>.
   * 
   * @parameter
   * @see http://www.erlang.org/doc/man/dialyzer.html
   */
  private String[] dialyzerOptions;

  /**
   * Issues {@code dialyzer:run/2} from source code on a specific set of sources
   * and includes.
   * 
   * @param sources to run dialyzer on
   * @param includes needed for the source files
   * @throws MojoFailureException
   * @throws MojoExecutionException
   */
  protected void executeDialyzer(List<File> sources, List<File> includes) throws MojoFailureException,
                                                                         MojoExecutionException {
    String sourceList = toStringList(sources, FILE_PRED);
    String includeList = toStringList(includes, FILE_PRED);
    String optionList = toPlainList(this.dialyzerOptions, null);
    String expression = String.format(script, sourceList, includeList, optionList);

    Log log = getLog();
    log.debug("about to evaluate " + expression);
    OtpErlangList warnings = (OtpErlangList) MavenSelf.get().eval(DEFAULT_PEER, expression);
    for (int i = 0; i < warnings.arity(); ++i) {
      OtpErlangTuple warning = (OtpErlangTuple) warnings.elementAt(i);
      OtpErlangObject file = warning.elementAt(0);
      OtpErlangObject line = warning.elementAt(1);
      OtpErlangObject msg = warning.elementAt(2);
      log.warn(file.toString() + ":" + line.toString() + ": " + msg.toString());
    }
    if (warnings.arity() > 0 && this.dialyzerWarningsAreErrors) {
      throw new MojoFailureException("dialyzer emitted warnings");
    }
  }
}
