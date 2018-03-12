@echo off

IF "%1"=="" GOTO USAGE
IF "%2"=="" GOTO USAGE
IF "%3"=="" GOTO CHECKFILE

:USAGE
echo Usage: %0 ^<NodeSet2.xml file^> ^<package^>
GOTO RETURN

:CHECKFILE
if exist %1 GOTO SETPACKAGE
else echo File '%1' does not exist.
GOTO RETURN

:SETPACKAGE
set FILE=%1
set PACKAGE=%2
set PACKAGEFILE=%FILE:~0,-4%.package
echo Setting package for file "%FILE%" to "%PACKAGE%" in file "%PACKAGEFILE%".
echo %PACKAGE% > %PACKAGEFILE%

:RETURN
