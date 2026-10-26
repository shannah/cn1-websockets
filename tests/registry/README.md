# Callback registry regression test

Run `tests/registry/run.sh` with Java 8 (`JAVA_HOME` may select the JDK).
No Maven downloads, server, simulator, or native build is needed.

The harness compiles the production WebSocket sources and uses the repository's
bundled CodenameOne.jar for the real NativeLookup and WeakHashMap. Test-only
Display/CN/Log adapters provide weak references, prohibit unexpected EDT dispatch,
and capture diagnostics; a fake native transport exposes IDs without networking.
These adapters are compiled only into a temporary directory, never the library.

Checks cover old-ID retirement and field updates on reconnect, all five callback
entry points, unknown-ID diagnostics, handlers that create sockets on another
thread, and 100 batches of 16 concurrent constructors/reconnects/callback streams.
The reconnect test fails deterministically on the original source; the concurrency
stress test additionally exercises the shared registry under contention.
This does not test native transport behavior or queued EDT callback lifecycle.

An optional first argument selects another source directory, e.g. the legacy demo:

```sh
tests/registry/run.sh "$PWD/cn1-websockets-demo/src/com/codename1/io/websocket"
```
