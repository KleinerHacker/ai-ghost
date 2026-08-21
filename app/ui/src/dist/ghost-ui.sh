#!/bin/sh
#
# Start script for the AI Ghost UI.
# All application and dependency JARs live next to this script in "libs".
#
set -e

APP_HOME=$(cd -P "$(dirname "$0")" && pwd)

if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD="java"
fi

exec "$JAVA_CMD" \
  --module-path "$APP_HOME/libs" \
  --add-modules ALL-MODULE-PATH \
  --module org.pcsoft.app.aighost.ui/org.pcsoft.app.aighost.app.LauncherKt \
  "$@"
