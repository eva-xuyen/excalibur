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
setlocal

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem
rem Find the application home.
rem
rem %~dp0 is name of current script under NT
set _REALPATH=%~dp0
set _WRAPPER_EXE=%_REALPATH%Wrapper.exe

rem Find the requested command.
for /F %%v in ('echo %1^|findstr "^console$ ^start$ ^stop$ ^restart$ ^install$ ^remove"') do call :exec set COMMAND=%%v

if "%COMMAND%" == "" (
    echo Usage: %0 { console : start : stop : restart : install : remove }
    pause
    goto :eof
) else (
    shift
)

rem
rem Find the wrapper.conf
rem
:conf
set _WRAPPER_CONF="%_REALPATH%..\conf\wrapper.conf"

rem
rem Run the application.
rem At runtime, the current directory will be that of Wrapper.exe
rem
call :%COMMAND%
if errorlevel 1 pause
goto :eof

:console
"%_WRAPPER_EXE%" -c %_WRAPPER_CONF%
goto :eof

:start
"%_WRAPPER_EXE%" -t %_WRAPPER_CONF%
goto :eof

:stop
"%_WRAPPER_EXE%" -p %_WRAPPER_CONF%
goto :eof

:install
"%_WRAPPER_EXE%" -i %_WRAPPER_CONF%
goto :eof

:remove
"%_WRAPPER_EXE%" -r %_WRAPPER_CONF%
goto :eof

:restart
call :stop
call :start
goto :eof

:exec
%*
goto :eof
