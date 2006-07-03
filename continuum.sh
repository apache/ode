#!/bin/sh
#
# Continuous integration build script for Continuum
# (does not yet support build.xml in subdirectory from checkout root)
#
cd PXE
exec ant $*