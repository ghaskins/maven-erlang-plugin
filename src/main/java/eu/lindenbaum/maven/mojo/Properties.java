package eu.lindenbaum.maven.mojo;

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;

/**
 * Represents a bean interface holding all values the plugin needs to work.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
interface Properties {
  /**
   * Returns the {@link MavenProject} to process.
   */
  public MavenProject project();

  /**
   * Returns the {@link ArtifactRepository} storing dependencies of this
   * {@link MavenProject}.
   */
  public ArtifactRepository repository();

  /**
   * Returns the name of the projects build artifact.
   */
  public String projectName();

  /**
   * Returns the name of the backend node to use.
   */
  public String node();

  /**
   * Returns the cookie that must be used when connecting to the backend node.
   */
  public String cookie();

  /**
   * Returns the directory where the application (upgrade) files reside.
   */
  public File ebin();

  /**
   * Returns the directory where the header files reside.
   */
  public File include();

  /**
   * Returns the directory where the private files reside.
   */
  public File priv();

  /**
   * Returns the directory where resources reside.
   */
  public File resources();

  /**
   * Returns the directory where the erlang sources reside.
   */
  public File src();

  /**
   * Returns the base folder for sources of this project. This may be used to
   * include sources from other languages into the erlang application.
   */
  public File src_base();

  /**
   * Returns the directory where the erlang test include files reside.
   */
  public File test_include();

  /**
   * Returns the directory where test resources reside.
   */
  public File test_resources();

  /**
   * Returns the directory where the erlang test source files reside.
   */
  public File test_src();

  /**
   * Returns the base directory for the build artifacts.
   */
  public File target();

  /**
   * Returns the directory where the compiled sources will be placed into.
   */
  public File targetEbin();

  /**
   * Returns the directory where includes to package will be put into.
   */
  public File targetInclude();

  /**
   * Returns the directories where dependencies get unpacked into.
   */
  public File targetLib();

  /**
   * Returns the directory where SNMP related resources will be put into.
   */
  public File targetMibs();

  /**
   * Returns the directory where private resources will be put into.
   */
  public File targetPriv();

  /**
   * Returns the base directory for the project packaging.
   */
  public File targetProject();

  /**
   * Returns the directoriy where all releases will be put into.
   */
  public File targetReleases();

  /**
   * Returns the directory where sources to package will be put into.
   */
  public File targetSrc();

  /**
   * Returns the directory where the surefire reports will be put into.
   */
  public File targetSurefireReports();

  /**
   * Returns the directory where the compiled test sources and recompiled
   * sources will be placed into.
   */
  public File targetTest();
}
