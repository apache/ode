@echo off
rem
rem    Licensed to the Apache Software Foundation (ASF) under one or more
rem    contributor license agreements.  See the NOTICE file distributed with
rem    this work for additional information regarding copyright ownership.
rem    The ASF licenses this file to You under the Apache License, Version 2.0
rem    (the "License"); you may not use this file except in compliance with
rem    the License.  You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem

if not "%OS%"=="Windows_NT" goto wrongOS

@setlocal

set command=%0
set progname=%~n0

set ODE_SERVER_HOME=%~dp0..
set ODE_SERVER_LIB=%~dp0..\lib
set ODE_SERVER_CONF=%~dp0..\conf

set ODE_SERVER_JAVAOPTS=%JAVA_OPTS%
set ODE_SERVER_JAVAOPTS=%ODE_SERVER_JAVAOPTS% -Dode.server.home=%ODE_SERVER_HOME% -Dderby.syste.home=%ODE_SERVER_HOME%
set ODE_SERVER_JAVAOPTS=%ODE_SERVER_JAVAOPTS% -Djava.naming.factory.initial=org.apache.openejb.core.LocalInitialContextFactory

if "%JAVA_HOME%"=="" goto noJavaHome
if not exist "%JAVA_HOME%"\bin\java.exe goto noJava

set JAVACMD="%JAVA_HOME%\bin\java.exe"

set LOCALCLASSPATH=%ODE_SERVER_CONF%;%ODE_SERVER_LIB%\*


%JAVACMD% %ODE_SERVER_JAVAOPTS% -cp %LOCALCLASSPATH% org.apache.ode.tomee.Main
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