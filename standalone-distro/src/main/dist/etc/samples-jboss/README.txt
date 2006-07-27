-------------------------------------------------
ODE Installation:

This directory provides two distinct methods of deploying ODE within
the JBoss application server (4.0+).  In both scenarios, the
following deployment conditions are required:

- 'ode.home' is a system property that must be set before starting the 
jboss server.  This can be done by setting the JAVA_OPTS enivornment variable.
Windows: set JAVA_OPTS="-Dode.home=c:/path/to/ode"
Unix: export JAVA_OPTS="-Dode.home=/path/to/ode"

- the 'jboss-odehsqldb-ds.xml' (ode datastore) must be deployed by
copying this file to the '$jboss/server/default' directory.

----------------------------------------------------
ODE Deployment:

- Deployment scenario 1: JBoss sar

Copy the ode.sar directory and all its content to the '$jboss/server/default'
directory.  This does NOT deploy the jetty web server, and relies on 
the tomcat web server distributed with JBoss.

- Deployment scenario 2: ODE kernel

Copy the 'jboss-odekernel-service.xml' to the '$jboss/server/default'
directory.  This mechanism deploys the single ODE kernel mbean, which
then defers to the ODE kernel configuration for loading the ODE module
mbeans (see jboss-odekernel-config.xml).  This DOES include the jetty
web server, which by default is set to listen on port 8090 (not 8080).