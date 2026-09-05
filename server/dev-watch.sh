#!/bin/sh
# Dev loop for server-dev: run the app and recompile on source change so
# Spring Boot DevTools restarts the JVM automatically (no manual restart).
set -eu

mvn -q -o compile >/dev/null 2>&1 || mvn -q compile || true
mvn spring-boot:run -Dspring-boot.run.profiles=dev &
APP_PID=$!

trap 'kill $APP_PID 2>/dev/null; wait $APP_PID 2>/dev/null || true; exit 0' INT TERM

if command -v inotifywait >/dev/null 2>&1; then
  while inotifywait -qr -e modify,create,delete,move \
      --exclude '(^|/)\.[^/]*' \
      src/main/java src/main/resources pom.xml; do
    echo "[dev-watch] change detected, recompiling..."
    mvn -q -o compile || echo "[dev-watch] compile failed, waiting for next change..."
  done
else
  echo "[dev-watch] inotifywait not found, running without auto-recompile."
  wait $APP_PID
fi
