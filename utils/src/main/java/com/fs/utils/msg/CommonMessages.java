/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils.msg;

public class CommonMessages extends MessageBundle {

  public String strError() {
    return this.format("Error");
  }

  public String strFatal() {
    return this.format("Fatal");
  }

  public String strInfo() {
    return this.format("Info");
  }

  public String strWarning() {
    return this.format("Warning");
  }

  public String msgFileNotFound(String string) {
    return this.format("File not found: {0}", string);
  }

  public String msgCannotWriteToFile(String string) {
    return this.format("Unable to write to file \"{0}\";"
        + " it may be a directory or otherwise unwritable.", string);
  }

  public String msgCannotReadFromFile(String string) {
    return this.format("Unable to read from file \"{0}\";"
        + " it may be missing, a directory, or otherwise unreadable.", string);
  }

}
