#!/bin/sh
# Dev loop for server-dev: recompile on source change so Spring Boot
# DevTools restarts the JVM. Stat-polling (not inotify) so edits from a
# Windows/Docker Desktop bind mount are always detected.
set -eu

POLL=2

scan() {
  find src/main/java src/main/resources pom.xml -type f -printf '%T@ %p\n' 2>/dev/null | sort
}

prune_stale_classes() {
  [ -d target/classes ] || return 0
  ( cd target/classes && for f in $(find . -name '*.class'); do
      j=${f%\$*}; j=${j%.class}.java
      [ -f "../../src/main/java/$j" ] || rm -f "$f"
    done )
}

# First build: fail loudly so the container shows why the app can't start.
mvn -o compile || mvn compile

mvn spring-boot:run -Dspring-boot.run.profiles=dev &
APP_PID=$!
trap 'kill $APP_PID 2>/dev/null; wait $APP_PID 2>/dev/null || true; exit 0' INT TERM

prev=$(scan)
while true; do
  sleep "$POLL"
  cur=$(scan)
  [ "$cur" = "$prev" ] && continue
  echo "[dev-watch] change detected, recompiling..."
  if mvn -q -o compile || mvn -q compile; then
    prune_stale_classes
    echo "[dev-watch] recompiled OK, DevTools will restart the app"
  else
    echo "[dev-watch] compile failed, keeping last good build..." >&2
  fi
  prev=$(scan)
done