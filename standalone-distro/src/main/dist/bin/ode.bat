@echo off

REM  -- Copyright (c) 1999-2003 FiveSight Technologies Inc.
REM  --  ALL RIGHTS RESERVED 

if not "%OS%"=="Windows_NT" goto wrongOS

@setlocal

set command=%0
set progname=%~n0

set ODE_HOME=%~dp0..
set ODE_BIN=%~dp0..\bin
set ODE_ETC=%~dp0..\etc
set ODE_LIB=%~dp0..\lib

if "%JAVA_HOME%"=="" goto noJavaHome
if not exist "%JAVA_HOME%"\bin\java.exe goto noJava

set JAVACMD="%JAVA_HOME%\bin\java.exe"
set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%ODE_CLASSPATH%

%JAVACMD% -server %ODE_JAVAOPTS% "-Dcom.sun.management.config.file=%ODE_ETC%\ode-management.properties" -Djava.system.class.loader=fivesight.bootstrap.BootLoader "-Dfivesight.bootstrap.BootLoader.basedir=%ODE_HOME%" "-Dfivesight.bootstrap.BootLoader.cfg=%ODE_ETC%\%progname%.cfg" "-Dcom.fs.progname=%progname%" -cp "%ODE_LIB%\ode-bootstrap.jar" org.apache.ode.utils.cli.Main "%ODE_ETC%\%progname%.cfg" %* 
goto end


=====================================================================
                              ERRORS
=====================================================================


:wrongOS
echo ERROR: ODE requires WindowsNT/XP. Aborting.
goto end

:noJavaHome
echo ERROR: JAVA_HOME not set! Aborting.
goto end

:noJava
echo ERROR: The Java VM (java.exe) was not found in %JAVA_HOME%\bin! Aborting
goto end

REM ================================================================
REM                             END
REM ================================================================
:end
@endlocal


