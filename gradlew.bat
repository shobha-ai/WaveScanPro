@echo off

rem Gradle Wrapper script for Windows

set GRADLE_HOME=%~dp0gradle
set GRADLE_WRAPPER_JAR=%GRADLE_HOME%\wrapper\gradle-wrapper.jar
set GRADLE_WRAPPER_PROPERTIES=%GRADLE_HOME%\wrapper\gradle-wrapper.properties

if not exist "%GRADLE_WRAPPER_JAR%" (
    echo Gradle Wrapper JAR not found at %GRADLE_WRAPPER_JAR%
    exit /b 1
)

java -jar "%GRADLE_WRAPPER_JAR%" --project-prop wrapper=true %*
