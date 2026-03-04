#!/bin/sh

set -eu

APP_HOME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "Missing gradle-wrapper.jar. Generate it with: gradle wrapper --gradle-version 8.10.2"
  exit 1
fi

exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
