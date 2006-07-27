/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.naming;

import org.apache.ode.utils.msg.MessageBundle;

import java.rmi.Naming;
import java.util.Hashtable;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;


/**
 * JNDI context factory for remote contexts implemented using the
 * <code>BindingMap</code> server.
 */
public class RemoteContextFactory implements InitialContextFactory {
  /**
   * Environment property containing a <code>BindingMap</code> object that
   * defines the root remote context.
   */
  public static final String PROVIDER_BINDINGMAP = "com.fs.naming.RemoteBindingMap";
  private static final NamingMessages __msgs = MessageBundle.getMessages(NamingMessages.class);

  /**
   * DOCUMENTME
   *
   * @param env DOCUMENTME
   *
   * @return DOCUMENTME
   *
   * @throws NamingException DOCUMENTME
   * @throws CommunicationException DOCUMENTME
   */
  public Context getInitialContext(Hashtable env)
                            throws NamingException {
    BindingMap mb = (BindingMap)env.get(PROVIDER_BINDINGMAP);

    if (mb != null) {
      return new BindingMapContextImpl(mb, env);
    }

    String providerURL = (String)env.get(Context.PROVIDER_URL);

    if (providerURL == null) {
      throw new NamingException(__msgs.msgInvalidProviderURL(providerURL));
    }

    if (providerURL.startsWith("rmi:")) {
      // The RMI case: Use Naming class to find the remote object.
      try {
        mb = (BindingMap)Naming.lookup(providerURL.substring(4));
      } catch (Exception re) {
        throw new CommunicationException(__msgs.msgConnectErr(providerURL));
      }

      return new BindingMapContextImpl(mb, env);
    } else if (providerURL.startsWith("jndi:")) {
      try {
        mb = (BindingMap)(new InitialContext()).lookup(providerURL.substring(5));
      } catch (Exception re) {
        throw new CommunicationException(__msgs.msgConnectErr(providerURL));
      }
    }

    throw new NamingException(__msgs.msgInvalidProviderURL(providerURL));
  }
}
