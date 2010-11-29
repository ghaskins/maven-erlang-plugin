package eu.lindenbaum.maven.mojo;

import static eu.lindenbaum.maven.util.ErlConstants.BEAM_SUFFIX;
import static eu.lindenbaum.maven.util.ErlConstants.TARGZ_SUFFIX;
import static eu.lindenbaum.maven.util.FileUtils.APP_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.getFilesRecursive;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.lindenbaum.maven.archiver.TarGzArchiver;
import eu.lindenbaum.maven.erlang.CheckAppResult;
import eu.lindenbaum.maven.erlang.CheckAppScript;
import eu.lindenbaum.maven.erlang.CheckAppUpScript;
import eu.lindenbaum.maven.erlang.FilterForAttributeScript;
import eu.lindenbaum.maven.erlang.GetAttributesScript;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;
import eu.lindenbaum.maven.util.ErlConstants;
import eu.lindenbaum.maven.util.ErlUtils;
import eu.lindenbaum.maven.util.MavenUtils;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * <p>
 * This {@link Mojo} packages all application artifacts into a single
 * {@code .tar.gz} package. This includes {@code .beam} files, the {@code .hrl}
 * include files, SNMP resources, private data from the {@code priv} and
 * {@code resources} directories and non-erlang sources.
 * </p>
 * <p>
 * Besides that this {@link Mojo} also copies the erlang application resource
 * file. In order to manage the project over the project pom there is the
 * possibility to let the {@link Mojo} automatically fill in values from the
 * project pom into the {@code .app} file. This can be done by using one of the
 * supported variables into the application resource files. Below is a list of
 * supported variables and their substitutions:
 * </p>
 * <ul>
 * <li><code>${ARTIFACT}</code>: the projects artifact id (atom)</li>
 * <li><code>${DESCRIPTION}</code>: the projects description (string)</li>
 * <li><code>${ID}</code>: the projects id (string)</li>
 * <li><code>${VERSION}</code>: the projects version (string)</li>
 * <li><code>${MODULES}</code>: all compiled {@code .beam} files found in the
 * target ebin folder (list)</li>
 * <li><code>${REGISTERED}</code>: all registered names, based on the
 * {@code -registered(Names).} attribute retrieved from the compiled
 * {@code .beam} files (list)</li>
 * </ul>
 * <p>
 * In case there is no application resouce file specified the {@link Mojo} will
 * generate a default {@code .app} file which looks like this:
 * </p>
 * 
 * <pre>
 * {application, ${ARTIFACT},
 *   [{description, ${DESCRIPTION}},
 *    {id, ${ID}},
 *    {vsn, ${VERSION}},
 *    {modules, ${MODULES}},
 *    {maxT, infinity},
 *    {registered, ${REGISTERED}},
 *    {included_applications, []},
 *    {applications, []},
 *    {env, []},
 *    {mod, undefined},
 *    {start_phases, []}]}.
 * </pre>
 * <p>
 * The resulting application resource file as well as the application upgrade
 * file will be checked for plausability regardless if generated or not. This is
 * done by checking the application version against the project version,
 * checking the application modules against the found compiled modules as well
 * as checking the application's start module.
 * </p>
 * 
 * @goal package
 * @phase package
 * @author Olivier Sambourg
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public final class Packager extends ErlangMojo {
  /**
   * Setting this to {@code true} will break the build when the application file
   * does not contain all found modules.
   * 
   * @parameter default-value="true"
   */
  private boolean failOnUndeclaredModules;

  @Override
  protected void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException {
    log.info(MavenUtils.SEPARATOR);
    log.info(" P A C K A G E R");
    log.info(MavenUtils.SEPARATOR);

    String projectVersion = p.project().getVersion();

    List<File> modules = getFilesRecursive(p.targetEbin(), ErlConstants.BEAM_SUFFIX);
    Script<String> registeredScript = new GetAttributesScript(p.targetEbin(), modules, "registered");
    String registeredNames = MavenSelf.get().eval(p.node(), registeredScript);

    Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("${ARTIFACT}", "\'" + p.project().getArtifactId() + "\'");
    replacements.put("${DESCRIPTION}", "\"" + p.project().getDescription() + "\"");
    replacements.put("${ID}", "\"" + p.project().getId() + "\"");
    replacements.put("${VERSION}", "\"" + projectVersion + "\"");
    replacements.put("${MODULES}", ErlUtils.toModuleList(modules, "'", "'"));
    replacements.put("${REGISTERED}", registeredNames);

    // copy application resource files
    p.targetEbin().mkdirs();
    int copied = copyDirectory(p.ebin(), p.targetEbin(), APP_FILTER, replacements);
    log.debug("Copied " + copied + " application resource files");

    File appFile = new File(p.targetEbin(), p.project().getArtifactId() + ErlConstants.APP_SUFFIX);
    if (!appFile.exists()) {
      log.error(appFile.getName() + " does not exist.");
      log.error("Use 'mvn erlang:setup' to create a default .app file");
      throw new MojoFailureException("no .app file found");
    }

    // check .app file
    Script<CheckAppResult> appScript = new CheckAppScript(appFile);
    CheckAppResult appResult = MavenSelf.get().eval(p.node(), appScript);

    String name = appResult.getName();
    if (!p.project().getArtifactId().equals(name)) {
      log.error("Name mismatch.");
      log.error("Project name is " + p.project().getArtifactId() + " while .app name is " + name);
      throw new MojoFailureException("name mismatch " + p.project().getArtifactId() + " != " + name);
    }

    String version = appResult.getVersion();
    if (!projectVersion.equals(version)) {
      log.error("Version mismatch.");
      log.error("Project version is " + projectVersion + " while .app version is " + version);
      throw new MojoFailureException("version mismatch " + projectVersion + " != " + version);
    }

    String startModule = appResult.getStartModule();
    if ("undefined".equals(startModule)) {
      log.info("No start module configured.");
      log.info("This is ok for library applications.");
    }
    else {
      File beamFile = new File(p.targetEbin(), startModule + ErlConstants.BEAM_SUFFIX);
      if (beamFile.isFile()) {
        List<File> list = Arrays.asList(beamFile);
        Script<String> behaviourScript = new FilterForAttributeScript(p.targetEbin(), list, "behaviour");
        String behaviours = MavenSelf.get().eval(p.node(), behaviourScript);
        if (behaviours.contains("application")) {
          if (!appResult.getApplications().contains("sasl")) {
            log.error("Application dependency to sasl is missing.");
            throw new MojoFailureException("dependency to sasl is missing");
          }
        }
        else {
          log.error("Configured start module \'" + startModule
                    + "\' does not implement the application behaviour");
          throw new MojoFailureException("Configured start module does not implement the application behaviour");
        }
      }
      else {
        log.error("Configured start module \'" + startModule + "\' does not exist.");
        throw new MojoFailureException("configured start module does not exist");
      }
    }

    appResult.getApplications();

    appResult.getModules();

    File appUpFile = new File(p.targetEbin(), p.project().getArtifactId() + ErlConstants.APPUP_SUFFIX);
    if (!appUpFile.exists()) {
      log.warn(appUpFile.getName() + " does not exist.");
      log.warn("Use 'mvn erlang:setup' to create a default .appup file");
    }
    else {
      // check .appup file
      Script<String> appUpScript = new CheckAppUpScript(appUpFile, projectVersion);
      String error = MavenSelf.get().eval(p.node(), appUpScript);
      if (error != null) {
        log.error(appUpFile.getAbsolutePath() + ":");
        log.error(error);
        throw new MojoFailureException(".appup file has errors");
      }
    }

    // create .tar.gz package
    File toFile = new File(p.targetProject().getAbsolutePath() + TARGZ_SUFFIX);
    try {
      TarGzArchiver archiver = new TarGzArchiver(p.node(), toFile);
      archiver.addFile(p.targetProject());
      archiver.createArchive();
      p.project().getArtifact().setFile(toFile);
    }
    catch (IOException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
    log.info(MavenUtils.SEPARATOR);
  }

//  /**
//   * Checks whether the modules to be packaged are declared in the erlang
//   * application file.
//   * 
//   * @param appFile the erlang application resource file
//   * @throws MojoExecutionException
//   * @throws MojoFailureException in case of undeclared modules, if
//   *           {@link #failOnUndeclaredModules}
//   */
//  private void checkModules(File appFile) throws MojoExecutionException, MojoFailureException {
//    Log log = getLog();
//    String name = this.project.getArtifactId();
//    String moduleStr = eval(log, String.format(EXTRACT_MODULES, name, appFile.getPath()));
//    Set<String> appModules = new HashSet<String>(Arrays.asList(moduleStr.split(" ")));
//    Set<String> modules = new HashSet<String>();
//    for (File beam : getFilesRecursive(this.targetEbin, BEAM_SUFFIX)) {
//      modules.add(beam.getName().replace(BEAM_SUFFIX, ""));
//    }
//    if (!modules.containsAll(appModules) || !appModules.containsAll(modules)) {
//      Set<String> undeclared = new HashSet<String>(modules);
//      undeclared.removeAll(appModules);
//      log.warn("Undeclared modules: " + undeclared.toString());
//      Set<String> unbacked = new HashSet<String>(appModules);
//      unbacked.removeAll(modules);
//      log.warn("Unbacked modules: " + unbacked.toString());
//      if (this.failOnUndeclaredModules) {
//        throw new MojoFailureException("Module mismatch found.");
//      }
//    }
//  }

  /**
   * Returns a {@link String} containing all source modules in the given
   * directory in valid erlang list representation.
   * 
   * @param directory to scan for sources
   * @return a {@link String} containing all found source modules
   */
  private static String getModules(File directory) {
    StringBuilder modules = new StringBuilder("[");
    List<File> sources = getFilesRecursive(directory, BEAM_SUFFIX);
    for (int i = 0; i < sources.size(); ++i) {
      if (i != 0) {
        modules.append(", ");
      }
      modules.append("\'" + sources.get(i).getName().replace(BEAM_SUFFIX, "") + "\'");
    }
    modules.append("]");
    return modules.toString();
  }
}
