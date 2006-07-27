/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import org.apache.ode.ra.transports.OdeTransport;

import java.util.Properties;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * Information about a connection.
 */
class OdeConnectionRequestInfo implements ConnectionRequestInfo {
  OdeTransport transport;
  String url;
  final Properties properties = new Properties();

  OdeConnectionRequestInfo(OdeTransport transport, String url) {
    this.transport = transport;
    this.url = url;
  }

  public int hashCode() {
    return url.hashCode();
  }

  public boolean equals(Object obj) {
    OdeConnectionRequestInfo other = (OdeConnectionRequestInfo) obj;
    return transport.equals(other.transport) && url.equals(other.url) &&
                   properties.equals(other.properties);
  }
}
