/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 * 
 */

package com.fs.pxe.kernel.modjmxremote;

import com.fs.utils.msg.MessageBundle;

public class Messages extends MessageBundle {

  public String msgErrorStartingJmxRemoting(String jmxURL) {
    return this.format("Unable to start JMX remoting service \"{0}\".", jmxURL);
  }

  public String msgErrorStoppingJmxRemoting(String jmxURL) {
    return this.format("Error stopping JMX remoting service \"{0}\".", jmxURL);
  }

  public String msgMalformedJMXServiceURL(String jmxURL) {
    return this.format("The JMX Service URL \"{0}\" is invalid;"
        + " check the \"jmxURL\" property.", jmxURL);
  }

  public String msgStartedJmxRemoting(String jmxURL) {
    return this.format("JMX remote server started on \"{0}\".", jmxURL);
  }

  public String msgMustSpecifyJmxURL() {
    return this.format("The \"JmxURL\" configuration property must be specified!");
  }

}
