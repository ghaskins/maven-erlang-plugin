package eu.lindenbaum.maven.mojo;

import java.io.File;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * A base class for all {@link Mojo}s that need to operate on values provided by
 * the {@link PropertiesImpl} bean.
 * 
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 * @see PackagingType
 * @see Properties
 */
abstract class ErlangMojo extends AbstractMojo {
  /**
   * {@link MavenProject} to process.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * {@link ArtifactRepository} storing dependencies of this
   * {@link MavenProject}.
   * 
   * @parameter expression="${localRepository}"
   * @required
   * @readonly
   */
  private ArtifactRepository repository;

  /**
   * The projects working directory root.
   * 
   * @parameter expression="${basedir}"
   * @required
   * @readonly
   */
  private File base;

  /**
   * The name of the backend node to use.
   * 
   * @parameter expression="${node}" default-value="maven-erlang-plugin-backend"
   * @required
   */
  private String node;

  /**
   * The name of the backend node to use.
   * 
   * @parameter expression="${cookie}" default-value=""
   * @required
   */
  private String cookie;

  /**
   * Injects the needed {@link Properties} into the abstract
   * {@link #execute(Properties)} method to be implemented by subclasses.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    PackagingType type = PackagingType.fromString(this.project.getPackaging());
    Properties p = new PropertiesImpl(type, this.project, this.repository, this.base, this.node, this.cookie);
    execute(getLog(), p);
  }

  /**
   * Will be invoked when {@link #execute()} gets invoked on the base class.
   * 
   * @param log logger to be used for output logging
   * @param p to be passed by the base class.
   * @see AbstractMojo#execute()
   */
  protected abstract void execute(Log log, Properties p) throws MojoExecutionException, MojoFailureException;

  /**
   * Implementation of the {@link Properties} bean that provides the directory
   * layout as described in {@link PackagingType}.
   * 
   * @see PackagingType
   */
  private class PropertiesImpl implements Properties {
    private final MavenProject project;
    private final ArtifactRepository repository;

    private final String projectName;
    private final PackagingType packagingType;
    private final String node;
    private final String cookie;

    private final File ebin;
    private final File include;
    private final File priv;
    private final File src;
    private final File src_base;
    private final File test_include;
    private final File test_priv;
    private final File test_src;

    private final File target;
    private final File targetEbin;
    private final File targetInclude;
    private final File targetLib;
    private final File targetMibs;
    private final File targetPriv;
    private final File targetProject;
    private final File targetReleases;
    private final File targetSrc;
    private final File targetSurefireReports;
    private final File targetTest;
    private final File targetTestEbin;
    private final File targetTestPriv;

    PropertiesImpl(PackagingType type,
                   MavenProject project,
                   ArtifactRepository repository,
                   File base,
                   String node,
                   String cookie) {
      this.project = project;
      this.repository = repository;

      this.projectName = project.getArtifactId() + "-" + project.getVersion();
      this.packagingType = type;
      this.node = node;
      this.cookie = cookie;

      switch (type) {
        case ERLANG_STD: {
          this.ebin = new File(base, "ebin");
          this.include = new File(base, "include");
          this.priv = new File(base, "priv");
          this.src = new File(base, "src");
          this.src_base = base;
          this.test_include = new File(base, "test_include");
          this.test_priv = new File(base, "test_priv");
          this.test_src = new File(base, "test_src");
          break;
        }
        case ERLANG_OTP: {
          this.ebin = new File(base, "src/main/erlang");
          this.include = new File(base, "src/main/include");
          this.priv = new File(base, "src/main/priv");
          this.src = this.ebin;
          this.src_base = new File(base, "src/main");
          this.test_include = new File(base, "src/test/include");
          this.test_priv = new File(base, "src/test/priv");
          this.test_src = new File(base, "src/test/erlang");
          break;
        }
        default: { // ERLANG_REL
          this.ebin = base;
          this.include = base;
          this.priv = base;
          this.src = base;
          this.src_base = base;
          this.test_include = base;
          this.test_priv = base;
          this.test_src = base;
          break;
        }
      }

      this.target = new File(base, "target");
      this.targetProject = new File(this.target, this.projectName);
      this.targetEbin = new File(this.targetProject, "ebin");
      this.targetInclude = new File(this.targetProject, "include");
      this.targetLib = new File(this.target, "lib");
      this.targetMibs = new File(this.targetProject, "mibs");
      this.targetPriv = new File(this.targetProject, "priv");
      this.targetReleases = new File(this.target, "releases");
      this.targetSrc = new File(this.targetProject, "src");
      this.targetSurefireReports = new File(this.target, "surefire-reports");
      this.targetTest = new File(this.target, "test-" + this.projectName);
      this.targetTestEbin = new File(this.targetTest, "ebin");
      this.targetTestPriv = new File(this.targetTest, "priv");
    }

    @Override
    public MavenProject project() {
      return this.project;
    }

    @Override
    public ArtifactRepository repository() {
      return this.repository;
    }

    @Override
    public String projectName() {
      return this.projectName;
    }

    @Override
    public PackagingType packagingType() {
      return this.packagingType;
    }

    @Override
    public String node() {
      return this.node;
    }

    @Override
    public String cookie() {
      return this.cookie;
    }

    @Override
    public File ebin() {
      return this.ebin;
    }

    @Override
    public File include() {
      return this.include;
    }

    @Override
    public File priv() {
      return this.priv;
    }

    @Override
    public File src() {
      return this.src;
    }

    @Override
    public File src_base() {
      return this.src_base;
    }

    @Override
    public File test_include() {
      return this.test_include;
    }

    @Override
    public File test_priv() {
      return this.test_priv;
    }

    @Override
    public File test_src() {
      return this.test_src;
    }

    @Override
    public File target() {
      return this.target;
    }

    @Override
    public File targetEbin() {
      return this.targetEbin;
    }

    @Override
    public File targetInclude() {
      return this.targetInclude;
    }

    @Override
    public File targetLib() {
      return this.targetLib;
    }

    @Override
    public File targetMibs() {
      return this.targetMibs;
    }

    @Override
    public File targetPriv() {
      return this.targetPriv;
    }

    @Override
    public File targetProject() {
      return this.targetProject;
    }

    @Override
    public File targetReleases() {
      return this.targetReleases;
    }

    @Override
    public File targetSrc() {
      return this.targetSrc;
    }

    @Override
    public File targetSurefireReports() {
      return this.targetSurefireReports;
    }

    @Override
    public File targetTest() {
      return this.targetTest;
    }

    @Override
    public File targetTestEbin() {
      return this.targetTestEbin;
    }

    @Override
    public File targetTestPriv() {
      return this.targetTestPriv;
    }
  }
}
