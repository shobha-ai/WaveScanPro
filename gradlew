#!/bin/sh

# Gradle Wrapper script for Unix

set -e

GRADLE_HOME=$(dirname "$0")/gradle
GRADLE_WRAPPER_JAR=$GRADLE_HOME/wrapper/gradle-wrapper.jar
GRADLE_WRAPPER_PROPERTIES=$GRADLE_HOME/wrapper/gradle-wrapper.properties

# Download gradle-wrapper.jar if it doesn't exist or is empty
if [ ! -s "$GRADLE_WRAPPER_JAR" ]; then
    echo "Downloading Gradle Wrapper JAR..."
    mkdir -p "$GRADLE_HOME/wrapper"
    curl -L -o "$GRADLE_WRAPPER_JAR" https://repo.gradle.org/artifactory/libs-release-bintray/gradle/wrapper/gradle-wrapper.jar
fi

exec java -jar "$GRADLE_WRAPPER_JAR" --project-prop wrapper=true "$@"
