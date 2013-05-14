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

set ODE_HOME=%~dp0..
set ODE_BIN=%~dp0..\bin
set ODE_LIB=%~dp0..\lib
set ODE_ETC=%~dp0..\etc

if "%JAVA_HOME%"=="" goto noJavaHome
if not exist "%JAVA_HOME%"\bin\java.exe goto noJava

set JAVACMD="%JAVA_HOME%\bin\java.exe"

set LOCALCLASSPATH=%ODE_CLASSPATH%;"%ODE_LIB%"
FOR %%c in ("%ODE_LIB%"\*.jar) DO (call :append_cp "%%c")

%JAVACMD% %ODE_JAVAOPTS% -cp %LOCALCLASSPATH% org.apache.ode.tools.sendsoap.cline.HttpSoapSender %*
goto end

:append_cp
set LOCALCLASSPATH=%LOCALCLASSPATH%;%1
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
