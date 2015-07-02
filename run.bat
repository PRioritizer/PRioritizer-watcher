@echo off
SETLOCAL

SET NAME=watcher
SET VERSION=1.0
SET SCALA_VERSION=2.11
SET D=%~dp0
SET JAR=%D%\target\scala-%SCALA_VERSION%\%NAME%-assembly-%VERSION%.jar
SET THREADS=2

IF NOT EXIST %JAR% GOTO JarNotFound
GOTO ValidArgs

:JarNotFound
ECHO JAR file does not exist.
ECHO Make sure you run `sbt assembly` to build the JAR executable.
GOTO end

:ValidArgs
SET PROPS=-Dfile.encoding=UTF8 -Dscala.concurrent.context.minThreads=%THREAD% -Dscala.concurrent.context.numThreads=%THREADS% -Dscala.concurrent.context.maxThreads=%THREADS%
java %PROPS% -jar %JAR%
:end

ENDLOCAL
