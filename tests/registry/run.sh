#!/bin/sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/../.." && pwd)
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT HUP INT TERM
JAVAC=${JAVA_HOME:+$JAVA_HOME/bin/}javac
JAVA=${JAVA_HOME:+$JAVA_HOME/bin/}java
JAR="$ROOT/cn1-websockets-demo/lib/CodenameOne.jar"
SOURCE=${1:-$ROOT/cn1-websockets-lib/common/src/main/java/com/codename1/io/websocket}
"$JAVAC" -source 8 -target 8 -cp "$JAR" -d "$OUT" \
    "$ROOT"/tests/registry/src/com/codename1/ui/*.java \
    "$ROOT"/tests/registry/src/com/codename1/io/*.java \
    "$ROOT"/tests/registry/src/com/codename1/io/websocket/*.java \
    "$SOURCE/WebSocket.java" "$SOURCE/WebSocketNativeImpl.java" "$SOURCE/WebSocketState.java"
"$JAVA" -cp "$OUT:$JAR" com.codename1.io.websocket.RegistryRaceTest
