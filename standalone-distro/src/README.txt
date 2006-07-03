Process Execution Engine ("PXE")
Copyright (c) 2003-2005 FiveSight Technologies, Inc.

The primary source of documentation for PXE is the on-line wiki at

  http://wiki.fivesight.com/pxe
  
For information about licensing, see LICENSE.txt.  For version information, see
PXE/buildnumber.properties.

To build PXE:

1) Ensure that JAVA_HOME points to a 1.5 JDK.
2) Unset ANT_HOME, as a specific Ant version is packaged with the distribution.
3) cd PXE
4) ../build/ant -Dskip.rinfo=

A ready-to-experiment with build of PXE will be in PXE/build~/stage, and .zip'd and
.tgz'd binary distributions will be in install~/installers.

Send questions, bug reports, and comments to support@fivesight.com.