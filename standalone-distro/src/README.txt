Process Execution Engine ("ODE")
Copyright (c) 2003-2005 FiveSight Technologies, Inc.

The primary source of documentation for ODE is the on-line wiki at

  http://wiki.fivesight.com/ode
  
For information about licensing, see LICENSE.txt.  For version information, see
ODE/buildnumber.properties.

To build ODE:

1) Ensure that JAVA_HOME points to a 1.5 JDK.
2) Unset ANT_HOME, as a specific Ant version is packaged with the distribution.
3) cd ODE
4) ../build/ant -Dskip.rinfo=

A ready-to-experiment with build of ODE will be in ODE/build~/stage, and .zip'd and
.tgz'd binary distributions will be in install~/installers.

Send questions, bug reports, and comments to support@fivesight.com.