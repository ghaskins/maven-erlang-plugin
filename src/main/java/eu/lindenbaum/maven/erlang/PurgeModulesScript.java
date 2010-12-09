package eu.lindenbaum.maven.erlang;

import com.ericsson.otp.erlang.OtpErlangObject;

/**
 * A {@link Script} that purges all modules currently loaded except the ones
 * loaded directly from the backends lib directory retrieved using
 * <code>code:lib_dir/0</code>.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class PurgeModulesScript implements Script<Void> {
  private static final String script = //
  "    C = code:lib_dir()," + //
      "lists:foreach(" + //
      "  fun({Mod, Path}) ->" + //
      "        case string:str(Path, C) of" + //
      "             1 ->" + //
      "                 ok;" + //
      "             _ ->" + //
      "                 code:purge(Mod)," + //
      "                 code:delete(Mod)," + //
      "                 code:purge(Mod)" + //
      "        end," + //
      "  end, code:all_loaded()).";

  public PurgeModulesScript() {
    // no params
  }

  @Override
  public String get() {
    return script;
  }

  /**
   * The result of the {@link Script} execution is ignored.
   */
  @Override
  public Void handle(OtpErlangObject result) {
    return null;
  }
}
