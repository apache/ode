/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package fivesight.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * <p>
 * Markable extensions of <code>URLClassLoader</code> for JMX purposes.
 * </p>
 */
public class BootLoader extends URLClassLoader implements BootLoaderMBean {
  
  private boolean _debug;
  
  public static final String CONFIGURATION_FILE = BootLoader.class.getName() + ".cfg";
  
  /**
   * Name of property containing any additional classpath. This classpath is in the standard
   * Java form, and is <em>not</em> relative to the "base" directory. This property is
   * generally used to pass through a classpath defined in an environment variable.
   */
  public static final String PROP_CPATH = BootLoader.class.getName() + ".classPath";
  
  /**
   * Property with the file name (relative to the ode.home directory) that should
   * be used to initilize system properties. 
   */
  public static final String PROP_SYSTEM_PROPERTY_FILE = 
    BootLoader.class.getName() + ".systemPropertiesFile";  
  
  /**
   * The name of the system property that is expected to contain the "base" directory.
   * The base directory is given relative to the directory in which the property file
   * is found and is used for resolving library paths.  This will be used to set the
   * value of the <code>ode.home</code> system property.
   */
  public static final String PROP_BASEDIR = BootLoader.class.getName() + ".basedir";

  public static final String PROP_ODEHOME = "ode.home";
  /**
   * same constant defined in UTILS RMIConstants but we cannot have dependencies on it
   */
  public static final String PROP_RMIURL = "ode.rmiurl";
  
  /**
   * Name of the system property containing a comma-delimited list of libraries to include
   * in the classpath. The list may include JAR files or directories. Each item in the
   * list is taken to be relative to the base directory (see {@link PROP_BASEDIR}).
   * Entries of the form <code>xxx/*</code> are interpreted as "all jar files in the xxx directory"
   */
  public static final String PROP_LIBS = BootLoader.class.getName() + ".libs";

  public static final String PROP_DEBUG = BootLoader.class.getName() + ".debug";
    
  public BootLoader(ClassLoader cl) {
    super(new URL[] {}, cl);
    configure();
  }
  
  public BootLoader() {
    super(new URL[] {});
    configure();
  }

  private void configure() {
    String cfgfile = System.getProperty(CONFIGURATION_FILE);
    if (cfgfile == null) {
      throw new IllegalStateException("BOOTSTRAP FAILURE: " + CONFIGURATION_FILE +
          " system property *must* be set.");
    }

    File propertyFile = new File(cfgfile);
    if (!propertyFile.exists()) {
      throw new IllegalArgumentException("BOOTSTRAP FAILURE: No such file " + propertyFile);
    }

    Properties props = new Properties();
    try {
      FileInputStream fis = new FileInputStream(propertyFile);
      try {
        props.load(fis);
      } finally {
        fis.close();
      }
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
    
    _debug = props.containsKey(PROP_DEBUG) || System.getProperties().containsKey(PROP_DEBUG);

    if (_debug)
      System.err.println("BOOTSTRAP: Using configuration file: " + propertyFile);


    File baseDir;
    try {
      String basedir = props.getProperty(PROP_BASEDIR);
      if (basedir != null)
        baseDir = new File(propertyFile.getCanonicalFile().getParentFile(),basedir).getCanonicalFile();
      else
        baseDir = new File(propertyFile.getCanonicalFile().getParentFile(),"../").getCanonicalFile();

      System.setProperty(PROP_BASEDIR,baseDir.getCanonicalPath());
      System.setProperty(PROP_ODEHOME,baseDir.getCanonicalPath());
    } catch (IOException ioex) {
      System.err.println("BOOTSTRAP FAILURE: Could not resolve base directory.");
      throw new RuntimeException(ioex);
    }
    
    if (_debug)
      System.err.println("BOOTSTRAP: Using base directory: " + baseDir);
    
    URL[] classpath;

    try {
      classpath = calculateClassPath(baseDir, props);
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }
    
    for (int i=0; i < classpath.length; ++i) {
      addURL(classpath[i]);
    }
    
    String sysPropFileName = props.getProperty(PROP_SYSTEM_PROPERTY_FILE);
    if (sysPropFileName != null) {
      Properties sysProps = new Properties();
      try {
        File sysPropFile = new File(baseDir, sysPropFileName);
        if (_debug)
          System.err.println("BOOTSTRAP: Using system property file: " + sysPropFile);

        FileInputStream fis = new FileInputStream(sysPropFile);
        try {
          sysProps.load(fis);
        } finally {
          fis.close();
        }
      } catch (IOException ioex) {
        System.err.println("BOOTSTRAP FAILURE: System property file " + sysPropFileName + " not found!");
        throw new RuntimeException(ioex);
      }
      
      
      // NOTE: we transfer only those properties in the property file that
      // have not been overriden on the command line.
      sysProps.keySet().removeAll(System.getProperties().keySet());
      System.getProperties().putAll(sysProps);
    }
  }
  
  private URL[] calculateClassPath(File root, Properties props) throws IOException {
    List<URL> urls = new ArrayList<URL>();
    
    if (_debug) {
      System.err.println("BOOTSTRAP BASEDIR: " + root.getCanonicalPath());
    }
    
    String libs = props.getProperty(PROP_LIBS);
    if (_debug) {
      System.err.println("BOOTSTRAP LIBRARY LIST: " + libs);
    }
    
    if (libs != null) {
      StringTokenizer stok = new StringTokenizer(libs,",",false);
      while(stok.hasMoreTokens()) {
        String lib = stok.nextToken();
        if (_debug) {
          System.err.println("BOOTSTRAP PROCESSING LIBRARY: " + lib);
        }
        genClassPath(urls, root, lib);
      }
    }

    String classPathProp = System.getProperty(PROP_CPATH);
    if (_debug) {
      System.err.println("BOOTSTRAP ADDITIONAL CLASSPATH: "  + classPathProp);
    }
    
    if (classPathProp != null) {
      StringTokenizer stok = new StringTokenizer(classPathProp, System.getProperty("path.separator"), false);
      while (stok.hasMoreTokens()) {
        File file = new File(stok.nextToken());
        if (_debug) {
          System.err.println("BOOTSTRAP ADDED URL: " + file.toURL());
        }
        urls.add(file.toURL());
      }
    }
    if (_debug) {
      System.err.println("BOOTSTRAP CLASSPATH: " + urls);
    }
    return urls.toArray(new URL[urls.size()]);
  }

  private void genClassPath(List<URL> urls, File root, String lib)
    throws MalformedURLException {
    
    if (lib.startsWith("/")) {
      // Go to the root directory.
      genClassPath(urls, new File("/"),lib.substring(1));
    } else if (lib.indexOf('/') != -1) {
      // We have a path, so take the directory and recurse down
      String dirname = lib.substring(0,lib.indexOf('/'));
      File dir = new File(root,dirname);
      if (dir.exists() && dir.isDirectory()) {
        genClassPath(urls, dir, lib.substring(lib.indexOf('/')+1));
      }
    } else if (lib.length() == 0) {
      // We have reached an end to our path: add it do the classpath
      if (_debug) {
        System.err.println("BOOTSTRAP ADDED URL: " + root.toURL());
      }
      urls.add(root.toURL());
    } else if (root.isDirectory()) {
      // We have a terminal name or wildcard, lets see.
      if (lib.indexOf('*')!=-1) {
        // We have a wild-card, so we'll need to scan the current directory
        String prefix = lib.substring(0,lib.indexOf('*'));
        String suffix = lib.substring(lib.indexOf('*')+1);
        
        File files[] = root.listFiles();
        // we need to alphabatize the results so we have
        // some control over classloading order
        SortedSet<File> orderedSet = new TreeSet<File>();
        for (File f : files) {
          String fileName = f.getName(); 
          if (fileName.endsWith(suffix) && fileName.startsWith(prefix))
            orderedSet.add(f);
        }

        for(File f : orderedSet) {
          if (_debug) {
            System.err.println("BOOTSTRAP ADDED URL: " + f.toURL());
          }
          urls.add(f.toURL()); 
        }
      } else {
        genClassPath(urls, new File(root, lib), "");
      }
    }    
  }
}
