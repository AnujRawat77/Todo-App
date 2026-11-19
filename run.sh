#!/usr/bin/env bash
# Run the packaged Todo app.
set -euo pipefail
cd "$(dirname "$0")"
JAR="target/todo-app-1.0.0.jar"
[ -f "$JAR" ] || { echo "JAR not found; running build.sh first"; ./build.sh; }
exec java -jar "$JAR" "$@"
