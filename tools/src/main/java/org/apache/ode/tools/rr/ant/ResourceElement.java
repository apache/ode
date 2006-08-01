/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools.rr.ant;

import org.apache.ode.utils.rr.ResourceRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;


/**
 * Sub-element of the {@link org.apache.ode.tools.rr.ant.RrTask} used to add
 * individual resources to the repository. This object maintains two pieces
 * of information: the logical name of the resource (it's URI within the
 * resource repository) and the physical location of the resource (either
 * a {@link File} or URL).
 */
public class ResourceElement implements RrOperation {
  
	/** Location of the resource (file) */
	private File _file;

	/** Location of the resource (URL) */
  private String _location;
  
  /** Optional URI. */
  private String _uri;


  /**
   * The physical location (URL) of the resource. This location must be
   * accessible through the standard Java {@link java.net.URL} mechanism.
   * @param url resource location (URL string)
   */
  public void setLocation(String url) {
    _location = url;
  }

  /**
   * Get the physical location of the resource.
   * @return resource location (URL string)
   */
  public String getLocation() {
    return _location;
  }

  /**
   * Set the logical name of the resource. The logical name, will be the "primary key"
   * of the resource in the repository; it must be in URI form.
   * @param uri logical name of the resource (URI string)
   */
  public void setUri(String uri) {
  	_uri = uri;
  }

  /**
   * Get the logical name of the resource.
   * @return logical name of the resource (URI string)
   */
  public String getUri() {
  	return _uri;
  }

  /**
   * Get the file containing the resource (as a file).
   * @return file containing the resource
   */
  public File getFile() {
  	return _file;
  }

  /**
   * Set the physical location of the resource to be a local file. This may
   * be used as an alternative to {@link #setLocation(String)}.
   * @param file
   */
  public void setFile(File file) {
  	_file = file;
  }

  public void execute(RrTask executingTask, ResourceRepositoryBuilder rrb)
    throws URISyntaxException, IOException {
    URL url;
    URI uri;

    if (_location != null) {
      url = new URL(_location);
      uri = url.toURI();
    } else if (_file != null) {
      url = _file.toURI().toURL();
      uri = _file.toURI();
    } else {
      throw new BuildException("Must specify a resource URL or file!");
    }

    if (_uri != null) {
      uri = new URI(_uri);
    }

    rrb.addURI(uri, url);
    executingTask.log("Added " + url + " as " + uri, Project.MSG_VERBOSE);
  }

}
