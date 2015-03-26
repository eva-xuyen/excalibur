@echo off
rem
rem Example start script.
rem
rem Author: Leif Mortenson [leif@tanukisoftware.com]
rem Author: Berin Loritsch [bloritsch@apache.org]

rem
rem Determine if JAVA_HOME is set and if so then use it
rem
if not "%JAVA_HOME%"=="" goto found_java

set EXAMPLE_JAVACMD=java
goto file_locate

:found_java
set EXAMPLE_JAVACMD=%JAVA_HOME%\bin\java

:file_locate

rem
rem Locate where the example is in filesystem
rem
if not "%OS%"=="Windows_NT" goto start

rem %~dp0 is name of current script under NT
set EXAMPLE_HOME=%~dp0

rem : operator works similar to make : operator
set EXAMPLE_HOME=%EXAMPLE_HOME:\bin\=%

:start

if not "%EXAMPLE_HOME%" == "" goto example_home

echo.
echo Warning: EXAMPLE_HOME environment variable is not set.
echo   This needs to be set for Win9x as it's command prompt
echo   scripting bites
echo.
goto end

:example_home
rem
rem build the runtime classpath
rem
set CP=%EXAMPLE_HOME%\lib\container.jar


set _LIBJARS=
for %%i in (%EXAMPLE_HOME%\lib\*.jar) do call %EXAMPLE_HOME%\bin\cpappend.bat %%i
if not "%_LIBJARS%" == "" goto run

echo Unable to set CLASSPATH dynamically.
goto end

:run
set CP=%CP%%_LIBJARS%

rem Run the example application
%EXAMPLE_JAVACMD% -Djava.compiler="NONE" -classpath "%CP%" org.apache.avalon.fortress.examples.extended.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
