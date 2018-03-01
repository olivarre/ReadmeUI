@echo off

ECHO.
ECHO ============================================================================================
ECHO USAGE:  ReadmeUI.cmd  "optional-path-to-32bit-java.exe"
ECHO ============================================================================================
ECHO.

REM 	(REO) DEFAULT TO EXPECTING EVERYTHING IN DIRECTORY OF THIS BATCH FILE
SET CMD-FILE-DIR=%~dsp0
SET CMD-FILE-DIR=%CMD-FILE-DIR:~0,-1%

REM 	(REO) TRY TO FIND JAVA WITH THE JAVA_HOME ENV VARIABLE, OR USE PATH TO JAVA.EXE IF PROVIDED
SET JAVA-EXE=%JAVA_HOME%\bin\java.exe
IF NOT '%1' == '' SET JAVA-EXE=%~1

REM 	(REO) CONFIGURE BINARY AND LIBRARY PATHS FOR JAVA
SET CLASS=ReadmeUI
SET BIN-DIR=%CMD-FILE-DIR%\bin
SET JAR-DIR=%CMD-FILE-DIR%\lib
SET JAVA-PARAMS=-Djava.ext.dirs="%JAR-DIR%" -cp "%BIN-DIR%" %CLASS%
SET JAVA-CMD="%JAVA-EXE%" %JAVA-PARAMS%

SET DOS-CMD=start "READMEUI" /D "%BIN-DIR%" /MIN cmd.exe /C "%JAVA-CMD%"

REM 	(REO) IF JAVA ISN'T CORRECTLY CONFIGURED, EXIT EARLY
IF NOT EXIST "%JAVA-EXE%" (
ECHO The path to "%JAVA-EXE%" does not exist.  Cannot start ReadmeUI.
ECHO.
ECHO Solution 1:  Provide the complete path to java.exe as a double-quoted argument 
ECHO.
ECHO Solution 2:  Set your JAVA_HOME environment variable to something like C:\JdkXX\jre
GOTO FINISH
)

REM 	(REO) DISPLAY COMMANDLINES
ECHO JAVA COMMAND:  %JAVA-CMD%
ECHO  DOS COMMAND:  %DOS-CMD%
ECHO.

ECHO Attempting to start ReadmeUI in separate DOS window...
ECHO.

REM 	(REO) INVOKE JAVA IN A SEPARATE DOS WINDOW AND FROM THE BIN DIRECTORY WITH CONFIGURED BINARY AND LIBRARY PATHS
%DOS-CMD%

:FINISH