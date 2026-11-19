#!/usr/bin/env bash
# Compile, run tests, and package the runnable fat JAR.
set -euo pipefail
cd "$(dirname "$0")"
mvn -B clean package
echo "Built: target/todo-app-1.0.0.jar"
