/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import com.fs.pxe.ra.transports.PxeTransport;

import java.util.Properties;

import javax.resource.spi.ConnectionRequestInfo;

/**
 * Information about a connection.
 */
class PxeConnectionRequestInfo implements ConnectionRequestInfo {
  PxeTransport transport;
  String url;
  final Properties properties = new Properties();

  PxeConnectionRequestInfo(PxeTransport transport, String url) {
    this.transport = transport;
    this.url = url;
  }

  public int hashCode() {
    return url.hashCode();
  }

  public boolean equals(Object obj) {
    PxeConnectionRequestInfo other = (PxeConnectionRequestInfo) obj;
    return transport.equals(other.transport) && url.equals(other.url) &&
                   properties.equals(other.properties);
  }
}
