#!/bin/sh
set -eu

export SPRING_DEVTOOLS_RESTART_ENABLED=false
MAVEN_FLAGS=-Dmaven.compiler.useIncrementalCompilation=false

mvn -q $MAVEN_FLAGS clean compile
touch /tmp/reload-mark

run_app() {
  exec mvn -q $MAVEN_FLAGS spring-boot:run -Dspring-boot.run.fork=false
}

run_app &
APP_PID=$!

trap 'kill "$APP_PID" 2>/dev/null || true; exit 0' TERM INT

while true; do
  if find src -type f -newer /tmp/reload-mark 2>/dev/null | grep -q . || [ pom.xml -nt /tmp/reload-mark ]; then
    touch /tmp/reload-mark
    if mvn -q $MAVEN_FLAGS clean compile >/tmp/reload-compile.log 2>&1; then
      kill "$APP_PID" 2>/dev/null || true
      wait "$APP_PID" 2>/dev/null || true
      run_app &
      APP_PID=$!
    fi
  fi
  sleep 2
done