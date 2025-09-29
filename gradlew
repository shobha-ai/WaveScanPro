#!/bin/sh

# Gradle Wrapper script for Unix

set -e

GRADLE_HOME=$(dirname "$0")/gradle
GRADLE_WRAPPER_JAR=$GRADLE_HOME/wrapper/gradle-wrapper.jar
GRADLE_WRAPPER_PROPERTIES=$GRADLE_HOME/wrapper/gradle-wrapper.properties

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
    echo "Gradle Wrapper JAR not found at $GRADLE_WRAPPER_JAR"
    exit 1
fi

exec java -jar "$GRADLE_WRAPPER_JAR" --project-prop wrapper=true "$@"
