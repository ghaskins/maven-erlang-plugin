package eu.lindenbaum.maven;

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * <p>
 * A base class for all {@link Mojo}s of this plugin. Its only use is to keep
 * all path definitions in a single place in order to be easily customizable.
 * The default directory layout looks like this:
 * </p>
 * 
 * <pre>
 *  BASE
 *    +-- src
 *    |     +-- main
 *    |     |     +-- erlang (*.erl, *.app, *.appup, *.mib, *.funcs, *.rel, *.relup)
 *    |     |     +-- include (*.hrl)
 *    |     |     +-- priv (*)
 *    |     |     +-- resources
 *    |     |     |     +-- priv (*)
 *    |     |    [+-- *] (other non-erlang source folders)
 *    |     +-- test
 *    |           +-- erlang (*.erl)
 *    |           +-- include (*.hrl)
 *    +-- target (.dialyzer.ok)
 *    |     +-- ebin (*.beam, *.app, *.appup, *.rel, *.relup)
 *    |     +-- include (*.hrl)
 *    |     +-- lib (dependency applications)
 *    |     +-- mibs (*.bin)
 *    |     +-- priv (*)
 *    |     +-- releases (previous/subsequent releases)
 *    |     +-- surefire-reports (TEST-*.xml)
 *    |     +-- test (*.beam, *.hrl)
 *    |    [+-- *_src] (non-erlang source folders)
 *    +-- pom.xml
 * </pre>
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
abstract class AbstractErlangMojo extends AbstractMojo {
  /**
   * {@link MavenProject} to process.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  MavenProject project;

  /**
   * {@link ArtifactRepository} storing dependencies of this
   * {@link MavenProject}.
   * 
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  ArtifactRepository repository;

  /**
   * The base folder for sources of this project. This may be used to include
   * sources from other languages into the erlang application. Default is:
   * {@code src/main}.
   * 
   * @parameter expression="${basedir}/src/main"
   * @required
   * @readonly
   */
  File srcMain;

  /**
   * Directory where the erlang source folder reside. It is also assumed that
   * the application specific application resource file and (if any) the release
   * files are located here. Default is: {@code src/main/erlang}.
   * 
   * @parameter expression="${basedir}/src/main/erlang"
   * @required
   * @readonly
   */
  File srcMainErlang;

  /**
   * Directory where the header files reside. Default is:
   * {@code src/main/include}.
   * 
   * @parameter expression="${basedir}/src/main/include"
   * @required
   * @readonly
   */
  File srcMainInclude;

  /**
   * Directory where the private files reside. Default is: {@code src/main/priv}
   * .
   * 
   * @parameter expression="${basedir}/src/main/priv"
   * @required
   * @readonly
   */
  File srcMainPriv;

  /**
   * Directory where resources reside. Default is: {@code src/main/resources}.
   * 
   * @parameter expression="${basedir}/src/main/resources"
   * @required
   * @readonly
   */
  File srcMainResources;

  /**
   * Directory where resources reside. Default is: {@code src/main/resources}.
   * 
   * @parameter expression="${basedir}/src/main/resources/priv"
   * @required
   * @readonly
   */
  File srcMainResourcesPriv;

  /**
   * Directory where the erlang test source files reside. Default is:
   * {@code src/test/erlang}.
   * 
   * @parameter expression="${basedir}/src/test/erlang/"
   * @required
   * @readonly
   */
  File srcTestErlang;

  /**
   * Directory where the erlang test include files reside. Default is:
   * {@code src/test/include}.
   * 
   * @parameter expression="${basedir}/src/test/include"
   * @required
   * @readonly
   */
  File srcTestInclude;

  /**
   * Base directory for the build artifacts. Default is: {@code target}.
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  File target;

  /**
   * Directories where dependencies get unpacked into. Default is:
   * {@code target/lib}.
   * 
   * @parameter expression="${project.build.directory}/lib/"
   * @required
   * @readonly
   */
  File targetLib;

  /**
   * Directory where the compiled test sources and recompiled sources will be
   * placed into. Default is: {@code target/test}.
   * 
   * @parameter expression="${project.build.directory}/test"
   * @required
   * @readonly
   */
  File targetTest;

  /**
   * Directories where all releases will be put into. Default is:
   * {@code target/releases}.
   * 
   * @parameter expression="${project.build.directory}/releases"
   * @required
   * @readonly
   */
  File targetReleases;

  /**
   * Directory where the surefire reports will be put into. Default is:
   * {@code target/surefire}.
   * 
   * @parameter expression="${project.build.directory}/surefire-reports"
   * @required
   * @readonly
   */
  File targetSurefireReports;

  /**
   * Base directory for the project packaging. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}"
   * @required
   * @readonly
   */
  File targetProject;

  /**
   * Directory where the compiled sources will be placed into. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION/ebin}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}/ebin/"
   * @required
   * @readonly
   */
  File targetEbin;

  /**
   * Directory where includes to package will be put into. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION/include}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}/include"
   * @required
   * @readonly
   */
  File targetInclude;

  /**
   * Directory where SNMP related resources will be put into. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION/mibs}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}/mibs"
   * @required
   * @readonly
   */
  File targetMibs;

  /**
   * Directory where private resources will be put into. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION/priv}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}/priv"
   * @required
   * @readonly
   */
  File targetPriv;

  /**
   * Directory where sources to package will be put into. Default is:
   * {@code target/$ARTIFACT_ID-$VERSION/src}.
   * 
   * @parameter expression=
   *            "${project.build.directory}/${project.artifactid}-${project.version}/src"
   * @required
   * @readonly
   */
  File targetSrc;
}
