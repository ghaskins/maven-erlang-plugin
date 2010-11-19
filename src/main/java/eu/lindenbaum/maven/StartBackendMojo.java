package eu.lindenbaum.maven;

import static eu.lindenbaum.maven.erlang.MavenSelf.DEFAULT_PEER;
import static eu.lindenbaum.maven.util.MavenUtils.SEPARATOR;

import java.io.IOException;
import java.util.ArrayList;

import com.ericsson.otp.erlang.OtpAuthException;
import com.ericsson.otp.erlang.OtpConnection;
import com.ericsson.otp.erlang.OtpErlangList;
import com.ericsson.otp.erlang.OtpPeer;
import com.ericsson.otp.erlang.OtpSelf;

import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.util.ErlConstants;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * {@link Mojo} that starts an erlang node {@link AbstractErlangMojo#peer} used
 * as a backend for rpcs made by the plugin. The node will only be started if it
 * is not already running. The node will be shutdown when the executing JVM
 * exits. This is done by a {@link Runtime#addShutdownHook(Thread)} which will
 * only be added <b>once</b> each JVM execution.
 * 
 * @goal start-backend
 * @phase initialize
 * @author Tobias Schlager <tobias.schlager@lindenbaum.eu>
 */
public class StartBackendMojo extends AbstractErlangMojo {
  /**
   * Setting this to {@code false} will leave the plugins backend node up and
   * running even if the executing jvm exits.
   * 
   * @parameter expression="${shutdownNode}" default-value=true
   */
  private boolean shutdownNode = true;

  /**
   * Static thread shutting down the running plugin backend.
   */
  private static final Thread shutdownHook = new Thread(new Runnable() {
    @Override
    public void run() {
      try {
        OtpConnection connection = MavenSelf.get().connect(DEFAULT_PEER);
        connection.sendRPC("erlang", "halt", new OtpErlangList());
        System.out.println("[INFO] Successfully shut down '" + DEFAULT_PEER + "'");
      }
      catch (Exception e) {
        System.out.println("[ERROR] Failed to shutdown '" + DEFAULT_PEER + "'");
        e.printStackTrace();
      }
      System.out.println("[INFO] " + SEPARATOR);
    }
  });

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();
    OtpPeer peer = new OtpPeer(DEFAULT_PEER);
    try {
      try {
        long serial = System.nanoTime();
        new OtpSelf("maven-erlang-plugin-startup-" + serial).connect(peer);
        log.debug("node " + peer + " is already running.");
      }
      catch (IOException e) {
        log.debug("starting " + peer + ".");
        ArrayList<String> command = new ArrayList<String>();
        command.add(ErlConstants.ERL);
        command.add("-boot");
        command.add("start_sasl");
        command.add("-sname");
        command.add(peer.alive());
        command.add("-detached");
        Process process = new ProcessBuilder(command).start();
        if (process.waitFor() != 0) {
          throw new MojoExecutionException("failed to start " + peer);
        }
        log.debug("node " + peer + " sucessfully started.");
      }
      if (this.shutdownNode) {
        try {
          Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
        catch (IllegalArgumentException e1) {
          log.debug("shutdown hook already registered.");
        }
      }
      else {
        log.info("Node " + peer + " will not be shutdown automatically.");
        log.info("To shutdown the node run 'mvn erlang:start-backend -DshutdownNode=true'");
      }
    }
    catch (IOException e) {
      throw new MojoExecutionException("failed to start " + peer, e);
    }
    catch (OtpAuthException e) {
      throw new MojoExecutionException("failed to connect to " + peer, e);
    }
    catch (InterruptedException e) {
      throw new MojoExecutionException("failed to start " + peer, e);
    }
  }
}