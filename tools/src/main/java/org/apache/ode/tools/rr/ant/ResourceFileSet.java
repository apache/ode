/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.ode.tools.rr.ant;

import org.apache.ode.utils.fs.TempFileManager;
import org.apache.ode.utils.rr.ResourceRepositoryBuilder;;
import org.apache.ode.utils.xml.capture.XmlDependencyScanner;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * Extension of ANT <code>fileset</code> that is used with {@link RrTask} to
 * specify a collection of resources that is to be included in the repository.
 *
 * <p>
 * In addition to the standard features of {@link FileSet} this class provides
 * a destination URI and a recursive flag. The destination URI (if specified)
 * makes the resources found through the fileset appear relative to an arbitrary
 * base URI in the resource repository. For example:
 * <pre>
 * &lt;rr ... &gt;
 *    &lt;rrfileset desturi="http://www.foo.com/bar/" dir="." includes="*.xml" /&gt;
 * &lt;/rr &gt;
 * </pre>
 * would have the effect of making a file "baz.wsdl" in the local directory appear
 * in the resource repository under URI "http://www.foo.com/bar/baz.wsdl".
 * </p>
 *
 * <p>
 * In addition to URI mapping, this class provides a recursion capability. When
 * the <code>recursive="yes"</code> attribute is given, each resource in the fileset
 * will be scanned for imports and each import will also be included in the resource
 * repository. The scanning mechanism is aware of XML Schema, WSDL, and BPEL
 * <code>import</code> (and <code>include</code>) statements.
 * </p>
 *
 */
public class ResourceFileSet extends FileSet implements RrOperation {
  /** Recursive (deep) resource imports. */
  private boolean _recursive;

  /** Alias (Destination) URI. */
  private String _destUri;


  /**
   * Get the destination URI root.
   * @return destination URI root
   */
  public String getDestUri() {
    return _destUri;
  }

  /**
   * Set the destination URI root.
   * @param desturi destination URI root
   */
  public void setDestUri(String desturi) {
    _destUri = desturi;
  }

  /**
   * Is the <code>recursive</code> flag set?
   * @return state of the <code>recursive</code> flag.
   */
  public boolean isRecursive() {
    return _recursive;
  }

  /**
   * Set the <code>recursive</code> flag.
   * @param recursive recursive flag.
   */
  public void setRecursive(boolean recursive) {
    _recursive = recursive;
  }

  public void execute(RrTask executingTask, ResourceRepositoryBuilder rrb) throws BuildException {

    URI destRoot = null;
    if (_destUri != null)
      try {
        destRoot = new URI(_destUri);
      } catch (URISyntaxException e) {
        TempFileManager.cleanup();
        log("Malformed destination URI: " + _destUri, Project.MSG_ERR);
        throw new BuildException("Malformed destination URI: "  + _destUri);
      }

    String[] files = this.getDirectoryScanner(this.getProject()).getIncludedFiles();
    File dir = this.getDir(this.getProject());

    URI srcRoot = dir.toURI();
    List<URI> todo = new LinkedList<URI>();
    XmlDependencyScanner scanner = new XmlDependencyScanner();
    for (int i=0; i < files.length; ++i) {
      File f = new File(dir,files[i]);
      if (this.isRecursive()) {
        scanner.process(f.toURI());
        if (scanner.isError()) {
          throw new BuildException("Error scanning " + f.toURI() + "; resources could not be loaded: " + scanner.getErrors().keySet());
        }
      } else {
        todo.add(f.toURI());
      }
    }

    if (this.isRecursive()) {
      todo.addAll(scanner.getURIs());
    }

    for (Iterator<URI> i = todo.iterator();i.hasNext(); ) {
      URI src = i.next();
      URI relative = srcRoot.relativize(src);
      URI dest = destRoot == null ? src : destRoot.resolve(relative);
      URL fu;
      try {
        fu = src.toURL();
      } catch (MalformedURLException mue) {
        log("Unrecognized URI: " + src,Project.MSG_ERR);
        if (executingTask.getFailOnError()) {
          throw new BuildException("Unrecognized URI: " + src);
        }
        continue;
      }
      try {
        rrb.addURI(dest,fu);
        log("Added " + src + " as " + dest,Project.MSG_VERBOSE);
      } catch (Exception zre) {
        log("Error writing resource " + dest + " to repository.", Project.MSG_ERR);
        if (executingTask.getFailOnError()) {
          throw new BuildException(zre);
        }
      }  
    }
  }
  
}
