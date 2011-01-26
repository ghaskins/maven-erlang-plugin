package eu.lindenbaum.maven.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import eu.lindenbaum.maven.Properties;
import eu.lindenbaum.maven.erlang.CheckAppResult;
import eu.lindenbaum.maven.erlang.CheckAppScript;
import eu.lindenbaum.maven.erlang.MavenSelf;
import eu.lindenbaum.maven.erlang.Script;

import org.apache.maven.plugin.MojoExecutionException;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class AutoDeps {

  private HashMap<String, CheckAppResult> detailsMap = new HashMap<String, CheckAppResult>();
  private DirectedGraph<String, DefaultEdge> apps = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

  public AutoDeps(Properties p, List<File> dirs) throws MojoExecutionException {

    /* Build a map of all visible applications and their details */
    for (File dir : dirs) {
      for (File appFile : FileUtils.getFilesRecursive(dir, ErlConstants.APP_SUFFIX)) {
        Script<CheckAppResult> script = new CheckAppScript(appFile);
        CheckAppResult result = MavenSelf.get(p.cookie()).exec(p.node(), script, new ArrayList<File>());
        detailsMap.put(result.getName().toUpperCase(), result);
      }
    }
  }
  
  /**
   * Adds the application to the graph for later analysis
   * 
   * @param app
   * @throws MojoExecutionException
   */
  public void add(String app) throws MojoExecutionException {

    CheckAppResult result = detailsMap.get(app.toUpperCase());
    if (result == null) throw new MojoExecutionException("App " + app + " cannot be resolved");

    List<String> deps = result.getApplications();
    
    if ((app.equals("kernel") || app.equals("stdlib")) != true) {
      /*
       * Add in our standard applications that everyone gets
       */
      deps.add("kernel");
      deps.add("stdlib");
    }

    apps.addVertex(app);
    for (String dep : deps) {
      apps.addVertex(dep);
      apps.addEdge(dep, app);
      
      /* recursively add each dependency to the tree */
      this.add(dep);
    }
  }

  /**
   * Ensure the graph is acyclic and then return the ordered list of applications required to satisfy the
   * supplied constraints
   * 
   * @return
   * @throws MojoExecutionException
   */
  public List<CheckAppResult> analyze() throws MojoExecutionException {
    CycleDetector<String, DefaultEdge> cd = new CycleDetector<String, DefaultEdge>(apps);
    
    if( cd.detectCycles()) throw new MojoExecutionException("Circular dependencies detected");
    
    List<CheckAppResult> result = new ArrayList<CheckAppResult>();
    TopologicalOrderIterator<String, DefaultEdge> iter = new TopologicalOrderIterator<String, DefaultEdge>(apps);
    
    while (iter.hasNext()) {
      String app = iter.next();
      CheckAppResult details = detailsMap.get(app.toUpperCase());
      if (details == null) throw new MojoExecutionException("App " + app + " version cannot be resolved");

      result.add(details);
    }
    
    return result;
  }
}

