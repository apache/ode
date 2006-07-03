/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package genbuild;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;


/**
 * enclosing_type class/interface.  DOCUMENT_ME
 */
public class DepFile
  extends Task {

  //~ Instance/static variables ...............................................

  private String _projlist;
  private String _workdir;

  //~ Methods .................................................................

  /**
   * DOCUMENTME
   * 
   * @param args DOCUMENTME
   * @throws Exception DOCUMENTME
   */
  public static void main(String[] args)
        throws Exception {

    if (args.length != 2) {
      System.err.println("usage: GenBuild projlist_file work_dir");
      System.exit(1);
    }

    
    StringTokenizer projects = new StringTokenizer(args[0],", \t\r\n" ,false);

    File workDir = new File(args[1]);

    if (!workDir.exists() || !workDir.isDirectory())
      throw new Exception("Invalid work dir: " + workDir);

    Workspace ws = new Workspace();
    String proj = null;

    while (projects.hasMoreTokens()) {
      proj = clean(projects.nextToken());

      if (proj.length() == 0 || proj.charAt(0) == '#')

        continue;

      ws.addModule(proj);
    }


    for (Iterator i = ws.getModules().iterator(); i.hasNext();) {

      String module = (String)i.next();
      File projdir = new File(workDir, module);

      if (!projdir.exists() || !projdir.isDirectory())

        continue;

      File ldep = new File(projdir, "build.properties");

      if (ldep.exists()) {

        FileInputStream fis = new FileInputStream(ldep);
        Properties props = new Properties();
        props.load(fis);
        fis.close();
        String deps = props.getProperty("deps.module");
        if(deps != null) {
          StringTokenizer stok = new StringTokenizer(deps,", \r\n\t", false);
          String dep = null;

          while (stok.hasMoreTokens()) {
            dep = clean(stok.nextToken());
            if(dep.length() == 0)
              continue;
            //ws.addLibDependency(module, dep);
            ws.addModuleDependency(module, dep);
          }
        }
      }

    }

    AntBuilder ab = new AntBuilder(ws);
    ab.generateBuildFile(new File(workDir, "build.xml"));
    ab.generateWorkspaceFile(new File(workDir, "workspace.xml"));
  }

  /**
   * DOCUMENTME
   * 
   * @param projlist DOCUMENTME
   */
  public void setProjlist(String projlist) {
    _projlist = projlist;
  }

  /**
   * DOCUMENTME
   * 
   * @param workdir DOCUMENTME
   */
  public void setWorkdir(String workdir) {
    _workdir = workdir;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute()
        throws BuildException {

    try {
      main(new String[] { _projlist, _workdir });
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  private static final String clean(String in) {
    in = in.replace('\n', ' ');
    in = in.replace('\r', ' ');
    in = in.replace('\t', ' ');
    in = in.trim();

    return in;
  }
}
