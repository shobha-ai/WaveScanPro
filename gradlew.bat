@echo off

rem Gradle Wrapper script for Windows

set GRADLE_HOME=%~dp0gradle
set GRADLE_WRAPPER_JAR=%GRADLE_HOME%\wrapper\gradle-wrapper.jar
set GRADLE_WRAPPER_PROPERTIES=%GRADLE_HOME%\wrapper\gradle-wrapper.properties

rem Download gradle-wrapper.jar if it doesn't exist or is empty
if not exist "%GRADLE_WRAPPER_JAR%" (
    echo Downloading Gradle Wrapper JAR...
    mkdir "%GRADLE_HOME%\wrapper"
    powershell -Command "Invoke-WebRequest -Uri https://repo.gradle.org/artifactory/libs-release-bintray/gradle/wrapper/gradle-wrapper.jar -OutFile %GRADLE_WRAPPER_JAR%"
)

java -jar "%GRADLE_WRAPPER_JAR%" --project-prop wrapper=true %*
