@echo off

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
set LOCALCLASSPATH=%ODE_CLASSPATH%

FOR %%c in (%ODE_LIB%\*.jar) DO set LOCALCLASSPATH=!LOCALCLASSPATH!;%%c

%JAVACMD% %ODE_JAVAOPTS% -cp "%LOCALCLASSPATH%" org.apache.ode.tools.bpelc.cline.BpelC %* 
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


