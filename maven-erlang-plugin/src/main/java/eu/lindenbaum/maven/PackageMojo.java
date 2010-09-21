package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.util.ErlUtils.eval;
import static eu.lindenbaum.maven.util.FileUtils.NULL_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.SOURCE_FILTER;
import static eu.lindenbaum.maven.util.FileUtils.copyDirectory;
import static eu.lindenbaum.maven.util.FileUtils.getDependencies;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eu.lindenbaum.maven.util.EDocUtils;
import eu.lindenbaum.maven.util.ErlConstants;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Package the Erlang project.
 * 
 * @goal package
 * @phase package
 * @author Olivier Sambourg
 */
public final class PackageMojo extends AbstractMojo {
  /**
   * Command to extract the version from the .app file.
   */
  private static final String EXTRACT_VERSION = "{ok, List} = file:consult(\"%s\"), "
                                                + "{value, {application, %s, Properties}} = lists:keysearch(%s, 2, List), "
                                                + "{value, {vsn, Version}} = lists:keysearch(vsn, 1, Properties), "
                                                + "io:format(Version), io:nl().";

  /**
   * Command to extract the modules from the .app file.
   */
  private static final String EXTRACT_MODULES = "{ok, List} = file:consult(\"%s\"), "
                                                + "{value, {application, %s, Properties}} = lists:keysearch(%s, 2, List), "
                                                + "{value, {modules, Modules}} = lists:keysearch(modules, 1, Properties), "
                                                + "lists:foreach(fun(Module) -> io:format(\"~s~n\", [Module]) end, Modules).";

  /**
   * Command to check the .appup file.
   */
  private static final String CHECK_APPUP = "{ok, List} = file:consult(\"%s\"), "
                                            + "{value, {\"%s\", UpFrom, DownTo}} = lists:keysearch(\"%s\", 1, List), "
                                            + "lists:foreach(fun({Version, ProcList}) -> "
                                            + "  lists:foreach(fun(ProcElement) -> true = is_tuple(ProcElement) end, ProcList) "
                                            + "end, UpFrom ++ DownTo)," + "io:format(\"ok\"), io:nl().";

  /**
   * Project to interact with.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Build directory.
   * 
   * @parameter expression="${project.build.directory}"
   * @readonly
   */
  private File buildDirectory;

  /**
   * Directories where dependencies are unpacked. This directory contains OTP applications (name-version
   * directories, with include and ebin sub directories).
   * 
   * @parameter expression="${project.build.directory}/lib/"
   * @required
   */
  private File libDirectory;

  /**
   * Directory where the beam files were created.
   * 
   * @parameter expression="${project.build.directory}/ebin"
   */
  private File beamDirectory;

  /**
   * Directory where the private files were created.
   * 
   * @parameter expression="${project.build.directory}/priv"
   */
  private File privDirectory;

  /**
   * Directory where the mibs files were copied.
   * 
   * @parameter expression="${project.build.directory}/mibs"
   */
  private File mibsDirectory;

  /**
   * Directory where the source files are located.
   * 
   * @parameter expression="${basedir}/src/main/erlang"
   */
  private File inputDirectory;

  /**
   * Directory where the documentation will be created.
   * 
   * @parameter expression="${project.build.directory}/doc"
   */
  private File docDirectory;

  /**
   * Include directory.
   * 
   * @parameter expression="${basedir}/src/main/include"
   */
  private File includeDirectory;

  /**
   * Resources directory.
   * 
   * @parameter expression="${basedir}/src/main/resources"
   */
  private File resourcesDirectory;

  /**
   * Target directory.
   * 
   * @parameter expression="${project.build.directory}/${project.build.finalName}"
   */
  private File targetDirectory;

  /**
   * Project version.
   * 
   * @parameter expression="${project.version}"
   * @readonly
   */
  private String version;

  /**
   * Final name.
   * 
   * @parameter expression="${project.build.finalName}"
   * @readonly
   */
  private String finalName;

  /**
   * Check that all modules are mentioned in the .app file and fail if some modules are not listed there.
   * 
   * @parameter default-value="true"
   */
  private boolean failOnUndeclaredModules;

  /**
   * Generate edoc documentation.
   * 
   * @parameter default-value="false"
   */
  private boolean useEdoc;

  /**
   * Application resource file (in src/ directory).
   * 
   * @parameter
   */
  private String applicationResourceFile;

  /**
   * Edoc options.
   * 
   * @parameter
   */
  private String[] edocOptions;

  /**
   * The Zip archiver.
   * 
   * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
   * @required
   */
  private ZipArchiver zipArchiver;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("------------------------------------------------------------------------");
    getLog().info("PACKAGING PROJECT");

    final File f = this.targetDirectory;
    if (!f.exists()) {
      f.mkdirs();
    }

    if (!this.docDirectory.exists() && this.useEdoc) {
      this.docDirectory.mkdirs();
    }

    final File doc = new File(f, "doc");
    if (this.docDirectory.exists()) {
      if (!doc.exists()) {
        doc.mkdirs();
      }
    }

    final File src = new File(f, "src");
    final File ebin = new File(f, "ebin");

    try {
      if (this.beamDirectory.exists() && this.beamDirectory.listFiles().length > 0) {
        if (!ebin.exists()) {
          ebin.mkdirs();
        }
        copyDirectory(this.beamDirectory, ebin, NULL_FILTER);
      }
      if (this.inputDirectory.exists()) {
        if (!src.exists()) {
          src.mkdirs();
        }
        copyDirectory(this.inputDirectory, src, SOURCE_FILTER);
        if (src.listFiles().length == 0) {
          src.delete();
        }
      }
      if (this.includeDirectory.exists() && this.includeDirectory.listFiles().length > 0) {
        final File include = new File(f, "include");
        if (!include.exists()) {
          include.mkdirs();
        }
        copyDirectory(this.includeDirectory, include, NULL_FILTER);
      }
      if (this.resourcesDirectory.exists()) {
        copyDirectory(this.resourcesDirectory, f, NULL_FILTER);
      }
      if (this.privDirectory.exists() && this.privDirectory.listFiles().length > 0) {
        final File priv = new File(f, "priv");
        if (!priv.exists()) {
          priv.mkdirs();
        }
        copyDirectory(this.privDirectory, priv, NULL_FILTER);
      }
      if (this.mibsDirectory.exists() && this.mibsDirectory.listFiles().length > 0) {
        final File mibs = new File(f, "mibs");
        if (!mibs.exists()) {
          mibs.mkdirs();
        }
        copyDirectory(this.mibsDirectory, mibs, NULL_FILTER);
      }

      String theApplicationName = this.project.getArtifactId();
      final File theApplicationResourceFile = new File(this.beamDirectory.getPath(), theApplicationName
                                                                                     + ".app");

      String theVersion;
      if (theApplicationResourceFile.exists()) {
        // Check the version.
        final String theCheckVersionExpr = String.format(EXTRACT_VERSION,
                                                         theApplicationResourceFile.getPath(),
                                                         theApplicationName,
                                                         theApplicationName);
        final String theAppVersion = eval(getLog(), theCheckVersionExpr, getDependencies(this.libDirectory));
        if (!this.version.equals(theAppVersion)) {
          getLog().error("Version mismatch. Project version is " + this.version + " while .app version is "
                         + theAppVersion);
          throw new MojoFailureException("Version mismatch");
        }
        theVersion = theAppVersion;

        // Check the list of modules.
        final String theModulesExpr = String.format(EXTRACT_MODULES,
                                                    theApplicationResourceFile.getPath(),
                                                    theApplicationName,
                                                    theApplicationName);
        final String theModules = eval(getLog(), theModulesExpr, getDependencies(this.libDirectory));
        final Set<String> theModulesSet = new HashSet<String>(Arrays.asList(theModules.split("\\n")));
        final File[] beamFiles = ebin.listFiles(new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.endsWith(ErlConstants.BEAM_SUFFIX);
          }
        });
        boolean listMismatch = false;
        for (File beamFile : beamFiles) {
          final String theCompiledModule = beamFile.getName();
          final String theCompiledModuleName = theCompiledModule.substring(0,
                                                                           theCompiledModule.length()
                                                                               - ErlConstants.BEAM_SUFFIX.length());
          if (!theModulesSet.remove(theCompiledModuleName)) {
            final String theErrorString = "Module " + theCompiledModuleName + " is not listed in .app file";
            if (this.failOnUndeclaredModules) {
              getLog().error(theErrorString);
              listMismatch = true;
            }
            else {
              getLog().warn(theErrorString);
            }
          }
        }
        for (String unknownModule : theModulesSet) {
          getLog().error("Module " + unknownModule + " does not exist");
          listMismatch = true;
        }
        if (listMismatch) {
          throw new MojoFailureException("Modules list mismatch");
        }

        // Check the .appup.
        final File theApplicationUpgradeFile = new File(this.beamDirectory.getPath(),
                                                        theApplicationName + ErlConstants.APPUP_SUFFIX);
        if (theApplicationUpgradeFile.exists()) {
          final String theCheckAppupExpr = String.format(CHECK_APPUP,
                                                         theApplicationUpgradeFile.getPath(),
                                                         theVersion,
                                                         theVersion);
          final String result = eval(getLog(), theCheckAppupExpr, getDependencies(this.libDirectory));
          if (!"ok".equals(result)) {
            getLog().error("Issue with .appup file : " + result);
            throw new MojoFailureException("Invalid .appup file.");
          }
        }
      }
      else {
        getLog().warn("No .app file was found");
        theVersion = this.version;
      }

      // Generate documentation
      if (this.useEdoc) {
        getLog().info("Generating documentation with Edoc");

        if (theApplicationResourceFile.exists()) {
          EDocUtils.generateAppEDoc(getLog(), theApplicationName, src, this.docDirectory, this.edocOptions);
        }
        else {
          EDocUtils.generateEDoc(getLog(), src, this.docDirectory, this.edocOptions);
        }
      }

      // Copy documentation.
      if (this.docDirectory.exists()) {
        copyDirectory(this.docDirectory, doc, NULL_FILTER);
        if (doc.listFiles().length == 0) {
          doc.delete();
        }
      }

      // Zip the project
      final File toFile = new File(this.buildDirectory, this.finalName + ".zip");
      this.zipArchiver.addDirectory(f, theApplicationName + "-" + theVersion + File.separator);
      this.zipArchiver.setDestFile(toFile);
      this.zipArchiver.createArchive();
      this.project.getArtifact().setFile(toFile);

    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not package the project", e);
    }
    catch (ArchiverException e) {
      throw new MojoExecutionException("Could not package the project", e);
    }
    getLog().info("------------------------------------------------------------------------");

  }
}