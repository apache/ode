/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.rr;

import com.fs.utils.msg.MessageBundle;

/**
 * Message bundle for ResourceRepository functionality.
 */
public class Messages extends MessageBundle {

  /**
   * A resource was added to a repository.
   * 
   * @param uri
   * @param src
   * @return Added resource [uri={0}, source={1}]
   */
  public String msgAddedResource(String uri, String src) {
    return this.format("Added resource [uri={0}, source={1}]", uri, src);
  }

  /**
   * Return a key identifying an InputStream as an entry.
   * 
   * <InputStream>
   */
  public String msgInputStream() {
    return this.format("<InputStream>");
  }

  /**
   * Fatal error while trying to add an unknown resource type.
   * 
   * @param uri
   * @param name
   * 
   * Unable to add a resource of type {1} to repository for URI {0}.
   */
  public String msgFatalUnknownType(String uri, String name) {
    return this.format("Unable to add a resource of type {1} to repository for URI {0}.", uri,
        name);
  }

  /**
   * A URI could not be opened.
   * 
   * @param uri
   *          the URI for the resource that we were trying to load
   * @param message
   *          a descriptive error message
   * 
   * Unable to open stream for URI {0}: {1}
   */
  public String msgUnableToOpenStream(String uri, String message) {
    return this.format("Unable to open stream for URI {0}: {1}", uri, message);
  }

  /**
   * Report an error about a URI without a registered resource.
   * 
   * @param uri
   *          the URI
   * 
   * No resource was registered for URI {0}.
   */
  public String msgFatalMissingResource(String uri) {
    return this.format("No resource was registered for URI {0}.");
  }

  /**
   * Report an error about not being able to write to the destination.
   * 
   * @param msg
   *          a descriptive error message
   * 
   * Unable to write to output: {0}
   */
  public String msgCannotWriteToOutput(String msg) {
    return this.format("Unable to write to output: {0}");
  }

  /**
   * Report an error that occurred while writing a resource to a repository.
   * 
   * @param uri
   *          the URI
   * @param message
   *          a descriptive error message
   * 
   * Error adding resource at URI {0} to the repository: {1}
   */
  public String msgErrorWritingResource(String uri, String message) {
    return this
        .format("Error adding resource at URI {0} to the repository: {1}", uri, message);
  }

  /**
   * Format a message about being unable to write the registry into the
   * manifest.
   * 
   * @param message
   *          a descriptive message about the IO error.
   * 
   * Unable to write registry into manifest for repository: {0}
   */
  public String msgErrorWritingRegistry(String message) {
    return this.format("Unable to write registry into manifest for repository: {0}", message);
  }

  /**
   * Format a message about a non-existent working directory.
   * 
   * @param path
   *          the path for the desired working directory
   * 
   * The directory {0} does not exist (and thus is not a suitable working
   * directory).
   */
  public String msgWorkingDirectoryDoesNotExist(String path) {
    return this.format("The directory {0} does not exist"
        + " (and thus is not a suitable working directory).", path);
  }

  /**
   * Format a message about a working directory that is a regular file instead
   * of a directory.
   * 
   * @param path
   *          the path of the desired working directory
   * 
   * The path {0} does not specify a directory (and thus is not a suitable
   * working directory).
   */
  public String msgWorkingDirectoryNotDirectory(String path) {
    return this.format("The path {0} does not specify a directory"
        + " (and thus is not a suitable working directory).", path);
  }

  /**
   * Format a message about a non-writable working directory.
   * 
   * @param path
   *          the path of the working directory
   * 
   * The directory {0} is not writable (and thus is not a suitable working
   * directory).
   */
  public String msgWorkingDirectoryNotWritable(String path) {
    return this.format("The directory {0} is not writable"
        + " (and thus is not a suitable working directory).", path);
  }

}
