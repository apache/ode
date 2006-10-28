/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Title: $RCSfile$
 *
 * $Log$
 * Revision 1.2  2005/09/12 13:47:10  holger
 * finally closing ODE-23
 *
 * Revision 1.1.1.1  2005/05/20 18:15:38  prb
 * Initial import of ODE source code.
 *
 * Revision 1.1  2004/04/27 18:49:44  jguinney
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2003/10/14 18:29:15  mszefler
 * Imported from old directory structure
 *
 * Revision 1.2  2002/02/13 19:37:57  jguinney
 * Added use of PropertyHelper for setting datasource properties.
 *
 */
/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector.jdbc;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * ManagedConnectionFactory implementation for XADataSource connections.  You
 * give it an XADataSource, user, and password and it generated connections.
 * Matches connections based on JDBC user and XADataSource.  Currently supports
 * managed mode only.
 *
 * <p>In an environment where you invoke this class directly, the preferred way
 * to configure it is to call setUserName, setPassword, and setXADataSource with
 * a configured XADataSource.  In an environment where this is deployed as a
 * RAR into a server and configured via a deployment descriptor, you'll need to use
 * setXADataSourceClass and setXADataSourceProperties or setXADataSourceJNDIName
 * instead of setXADataSource.  The XADataSourceProperties value should be a
 * string in the format <tt>name=value;name=value;name=value...</tt> where the
 * names and values are properties of your XADataSource implementation.  For
 * example:</p>
 *
 * <pre>
 *     xaDataSourceJNDIName  java:jdbc/SomeXADataSource
 *                        - or -
 *        xaDataSourceClass  com.dbproduct.XADataSourceImpl
 *   xaDataSourceProperties  url=jdbc:dbproduct:config;port=9999
 * </pre>
 *
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 245 $
 */
public class XAManagedConnectionFactory implements ManagedConnectionFactory {
    private static final long serialVersionUID = 243112925271551140L;
    
    private transient PrintWriter logger;
    private transient XADataSource source;
    private String username;
    private String password;
    private String xaDataSourceClass;
    private String xaDataSourceProperties;
    private String xaDataSourceName;

    public XAManagedConnectionFactory() {
    }
    public Object createConnectionFactory(ConnectionManager mgr) throws javax.resource.ResourceException {
        DataSource source = new JDBCDataSource(mgr, this);
        if(logger != null) {
            try {
                source.setLogWriter(logger);
            } catch(SQLException e) {/*ignore*/}
        }
        return source;
    }
    public Object createConnectionFactory() throws javax.resource.ResourceException {
        throw new java.lang.UnsupportedOperationException("Must be used in managed mode");
    }

    public ManagedConnection createManagedConnection(Subject sub, ConnectionRequestInfo info) throws javax.resource.ResourceException {
        // Set user and password to default
        String user = username;
        String pw = password;

        // Check passed Subject and ConnectionRequestInfo for user/password overrides
        if(sub != null) {
            Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
            for(Iterator it = creds.iterator(); it.hasNext(); ) {
                PasswordCredential pc = (PasswordCredential)it.next();
                user = pc.getUserName();
                pw = new String(pc.getPassword());
                break;
            }
        } else {
            if(info != null) {
                JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
                user = jdbcInfo.user;
                pw = jdbcInfo.password;
            }
        }

        // Create the connection
        try {
            XAConnection con = getSource().getXAConnection(user, pw);
            ManagedConnection mc = new XAManagedConnection(source, con, user);
            mc.setLogWriter(logger);
            return mc;
        } catch(SQLException e) {
            throw new ResourceException("Unable to create DB XAConnection: "+e);
        }
    }

    public ManagedConnection matchManagedConnections(Set cons, Subject sub, ConnectionRequestInfo info) throws javax.resource.ResourceException {
        // Set user and password to default
        String user = username;

        // Check passed Subject and ConnectionRequestInfo for user/password overrides
        if(sub != null) {
            Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
            for(Iterator it = creds.iterator(); it.hasNext(); ) {
                PasswordCredential pc = (PasswordCredential)it.next();
                user = pc.getUserName();
                break;
            }
        } else {
            if(info != null) {
                if(!(info instanceof JDBCConnectionRequestInfo)) {
                    throw new ResourceException("Passed ConnectionRequestInfo class '"+info.getClass().getName()+"' to XAManagedConnectionFactory!");
                }
                JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
                user = jdbcInfo.user;
            }
        }

        // Check the connections in the Set
        for(Iterator it = cons.iterator(); it.hasNext(); ) {
            Object unknown = it.next();
            if(!(unknown instanceof XAManagedConnection)) {
                continue;
            }
            XAManagedConnection con = (XAManagedConnection)unknown;
            if(con.getUser().equals(user) && con.getDataSource().equals(source)) {
                return con;
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter writer) throws javax.resource.ResourceException {
        logger = writer;
    }
    public PrintWriter getLogWriter() throws javax.resource.ResourceException {
        return logger;
    }

    private XADataSource getSource() {
        if(source != null)
            return source;
        if(xaDataSourceClass != null) {
            try {
                Class cls = Class.forName(xaDataSourceClass);
                XADataSource xads = (XADataSource)cls.newInstance();
//                Properties props = parseProperties();
//                Iterator it = props.keySet().iterator();
//                while(it.hasNext()) {
//                    String name = null, value = null;
//                    try {
//                        name = (String)it.next();
//                        value = props.getProperty(name);
//                        org.apache.ode.utils.beans.PropertyHelper ph = 
//                          new org.apache.ode.utils.beans.PropertyHelper(cls);
//                        ph.setProperty(xads, name, value);
//                        /*Method meth = cls.getMethod("set"+Character.toUpperCase(name.charAt(0))+name.substring(1),
//                                                    new Class[]{String.class});
//                        meth.invoke(xads, new Object[]{value});*/
//                    } catch(Exception e) {
//                        if(logger != null) {
//                            logger.println("Unable to set XADataSource property "+name+"="+value+":");
//                        }
//                    }
//                }
                source = xads;
                return source;
            } catch(Exception e) {
                if(logger != null) {
                    logger.println("Unable to create and initialize XADataSource:");
                    e.printStackTrace(logger);
                }
            }
        } else if(xaDataSourceName != null) {
            try {
                InitialContext ic = new InitialContext();
                source = (XADataSource)ic.lookup(xaDataSourceName);
                return source;
            } catch(Exception e) {
                if(logger != null) {
                    logger.println("Unable to reach XADataSource in JNDI:");
                    e.printStackTrace(logger);
                }
            }
        }
        return null;
    }

    public void setUserName(String username) {this.username = username;}
    public String getUserName() {return username;}
    public void setPassword(String password) {this.password = password;}
    public String getPassword() {return password;}
    public void setXADataSource(XADataSource source) {this.source = source;}
    public XADataSource getXADataSource() {return source;}
    public void setXADataSourceClass(String className) {xaDataSourceClass = className;}
    public String getXADataSourceClass() {return xaDataSourceClass;}
    public void setXADataSourceProperties(String props) {xaDataSourceProperties = props;}
    public String getXADataSourceProperties() {return xaDataSourceProperties;}
    public void setXADataSourceJNDIName(String name) {xaDataSourceName = name;}
    public String getXADataSourceJNDIName() {return xaDataSourceName;}

//    private Properties parseProperties() {
//        Properties props = new Properties();
//
//        StringTokenizer tokens = new StringTokenizer(xaDataSourceProperties, ";=");
//
//        while (tokens.hasMoreTokens()) {
//            String key = tokens.nextToken();
//            String value = tokens.nextToken();
//            props.put(key, value);
//        }
//        return props;
//    }

}
