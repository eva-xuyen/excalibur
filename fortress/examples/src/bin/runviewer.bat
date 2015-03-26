@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

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
%EXAMPLE_JAVACMD% -Djava.compiler="NONE" -classpath "%CP%" org.apache.avalon.fortress.examples.viewer.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:end
