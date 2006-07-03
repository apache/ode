-------------------------------------------------
PXE Installation:

This directory provides two distinct methods of deploying PXE within
the JBoss application server (4.0+).  In both scenarios, the
following deployment conditions are required:

- 'pxe.home' is a system property that must be set before starting the 
jboss server.  This can be done by setting the JAVA_OPTS enivornment variable.
Windows: set JAVA_OPTS="-Dpxe.home=c:/path/to/pxe"
Unix: export JAVA_OPTS="-Dpxe.home=/path/to/pxe"

- the 'jboss-pxehsqldb-ds.xml' (pxe datastore) must be deployed by
copying this file to the '$jboss/server/default' directory.

----------------------------------------------------
PXE Deployment:

- Deployment scenario 1: JBoss sar

Copy the pxe.sar directory and all its content to the '$jboss/server/default'
directory.  This does NOT deploy the jetty web server, and relies on 
the tomcat web server distributed with JBoss.

- Deployment scenario 2: PXE kernel

Copy the 'jboss-pxekernel-service.xml' to the '$jboss/server/default'
directory.  This mechanism deploys the single PXE kernel mbean, which
then defers to the PXE kernel configuration for loading the PXE module
mbeans (see jboss-pxekernel-config.xml).  This DOES include the jetty
web server, which by default is set to listen on port 8090 (not 8080).