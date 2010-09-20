package eu.lindenbaum.maven.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Defines Erlang related constants.
 */
public interface ErlConstants {
  /**
   * Name of the erlang interpreter binary.
   */
  public static final String ERL = "erl";

  /**
   * Name of the erlang compiler.
   */
  public static final String ERLC = "erlc";

  /**
   * Name of the dialyzer tool.
   */
  public static final String DIALYZER = "dialyzer";

  /**
   * Suffix for erlang source files.
   */
  public static final String ERL_SUFFIX = ".erl";

  /**
   * Suffix for erlang header files.
   */
  public static final String HRL_SUFFIX = ".hrl";

  /**
   * Suffix for application resource files.
   */
  public static final String APP_SUFFIX = ".app";

  /**
   * Suffix for application upgrade files.
   */
  public static final String APPUP_SUFFIX = ".appup";

  /**
   * Suffix for mibs.
   */
  public static final String MIB_SUFFIX = ".mib";

  /**
   * Suffix for funcs (mibs handlers).
   */
  public static final String FUNCS_SUFFIX = ".funcs";

  /**
   * Suffix for mibs binaries
   */
  public static final String BIN_SUFFIX = ".bin";

  /**
   * Suffix for erlang binary files.
   */
  public static final String BEAM_SUFFIX = ".beam";

  /**
   * Suffix for rel files.
   */
  public static final String REL_SUFFIX = ".rel";

  /**
   * Suffix for eunit tests.
   */
  public static final String TEST_SUFFIX = "_test" + BEAM_SUFFIX;

  /**
   * Name of the coverdata binary (coverdata) file.
   */
  public static final String COVERDATA_BIN = "coverdata.coverdata";

  /**
   * Name of the dialyzer ok file.
   */
  public static final String DIALYZER_OK = ".dialyzer.ok";

  /**
   * Type of artifacts for applications, i.e. zip archive containing an erlang-otp application.
   */
  public static final String ARTIFACT_TYPE_OTP = "erlang-otp";

  /**
   * Type of artifacts for releases, i.e. tar gz archive containing an erlang-otp release.
   */
  public static final String ARTIFACT_TYPE_REL = "erlang-rel";

  /**
   * Name of the directory that contains the beam files.
   */
  public static final String EBIN_DIRECTORY = "ebin";

  /**
   * Name of the directory that contains the include files.
   */
  public static final String INCLUDE_DIRECTORY = "include";

  /**
   * Regex for the OTP application module directories.
   */
  public static final Pattern OTP_DIRECTORY_REGEX = Pattern.compile("([^-]+)-([^-]+)");

  /**
   * Filename filter to filter source files (.erl & .hrl). Return true if the given file is a source file (and
   * therefore should be included in the src/ directory in the package). Uses the file suffix.
   */
  public static final FilenameFilter SOURCE_FILENAME_FILTER = new FilenameFilter() {
    public boolean accept(File dir, String name) {
      return name != null && (name.endsWith(HRL_SUFFIX) || name.endsWith(ERL_SUFFIX));
    }
  };
}