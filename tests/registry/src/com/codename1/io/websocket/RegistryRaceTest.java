package com.codename1.io.websocket;

import com.codename1.io.Log;
import com.codename1.system.NativeLookup;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/** Exercises the real WebSocket, NativeLookup and CN1 WeakHashMap with a fake transport. */
public final class RegistryRaceTest {
    public static class FakeNative implements WebSocketNativeImpl {
        static final ThreadLocal<FakeNative> latest = new ThreadLocal<FakeNative>();
        int id;
        public FakeNative() { latest.set(this); }
        public void setId(int value) { id = value; Thread.yield(); }
        public int getId() { return id; }
        public boolean isSupported() { return true; }
        public String getProtocols() { return null; }
        public void setProtocols(String protocols) { }
        public void setUrl(String url) { }
        public void sendBytes(byte[] message) { }
        public void sendString(String message) { }
        public void close() { }
        public void connect() { }
        public int getReadyState() { return 3; }
    }

    static final class Socket extends WebSocket {
        int opens, texts, binaries, closes, errors;
        Runnable onOpenAction;
        Socket() { super("ws://localhost/test"); setCallbacksOnEdt(false); }
        protected void onOpen() { opens++; if (onOpenAction != null) onOpenAction.run(); }
        protected void onMessage(String value) { texts++; }
        protected void onMessage(byte[] value) { binaries++; }
        protected void onClose(int code, String reason) { closes++; }
        protected void onError(Exception ex) { errors++; }
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    static Map<?, ?> registry() throws Exception {
        Field f = WebSocket.class.getDeclaredField("sockets");
        f.setAccessible(true);
        return (Map<?, ?>) f.get(null);
    }

    static void concurrentRouting() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(16);
        Set<Integer> ids = new HashSet<Integer>();
        try {
            for (int round = 0; round < 100; round++) {
                final CountDownLatch start = new CountDownLatch(1);
                List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
                for (int i = 0; i < 16; i++) {
                    futures.add(pool.submit(new Callable<Integer>() {
                        public Integer call() throws Exception {
                            start.await();
                            Socket socket = new Socket();
                            int id = FakeNative.latest.get().id;
                            WebSocket.openReceived(id);
                            WebSocket.messageReceived(id, "hello");
                            WebSocket.messageReceived(id, new byte[] {1});
                            WebSocket.errorReceived(id, "test", 500);
                            // Race reconnect registration against other constructors and callbacks.
                            socket.reconnect();
                            int replacementId = FakeNative.latest.get().id;
                            WebSocket.openReceived(id);
                            WebSocket.openReceived(replacementId);
                            WebSocket.closeReceived(replacementId, 1000, "done");
                            check(socket.opens == 2 && socket.texts == 1 && socket.binaries == 1
                                    && socket.errors == 1 && socket.closes == 1,
                                    "Callback missing or delivered to another socket: " + id);
                            return id;
                        }
                    }));
                }
                start.countDown();
                for (Future<Integer> future : futures) {
                    check(ids.add(future.get(10, TimeUnit.SECONDS)), "Duplicate connection ID");
                }
            }
            check(registry().isEmpty(), "Closed connections remain registered");
        } finally {
            pool.shutdownNow();
        }
    }

    static void reconnectRetiresOldId() throws Exception {
        Socket socket = new Socket();
        int oldId = FakeNative.latest.get().id;
        socket.reconnect();
        int newId = FakeNative.latest.get().id;
        check(oldId != newId, "Reconnect reused an ID");
        Field id = WebSocket.class.getDeclaredField("id");
        id.setAccessible(true);
        check(id.getInt(socket) == newId, "Socket ID was not updated");
        check(!registry().containsKey(oldId), "Previous connection is still registered");
        WebSocket.openReceived(oldId);
        WebSocket.messageReceived(oldId, "stale");
        WebSocket.messageReceived(oldId, new byte[] {1});
        WebSocket.errorReceived(oldId, "stale", 500);
        WebSocket.closeReceived(oldId, 1000, "stale");
        check(socket.opens + socket.texts + socket.binaries + socket.errors + socket.closes == 0,
                "Retired connection delivered a callback");
        check(Log.messages.size() >= 5, "Missing unknown-ID diagnostics");
        WebSocket.openReceived(newId);
        WebSocket.closeReceived(newId, 1000, "done");
        check(socket.opens == 1 && socket.closes == 1, "Replacement callbacks lost");
        check(registry().isEmpty(), "Reconnect leaked a registry entry");
    }

    static void handlersDoNotHoldRegistryLock() throws Exception {
        Socket socket = new Socket();
        int id = FakeNative.latest.get().id;
        final ExecutorService pool = Executors.newSingleThreadExecutor();
        socket.onOpenAction = new Runnable() {
            public void run() {
                try {
                    pool.submit(new Runnable() {
                        public void run() {
                            Socket nested = new Socket();
                            WebSocket.closeReceived(FakeNative.latest.get().id, 1000, "done");
                            check(nested.closes == 1, "Nested callback lost");
                        }
                    }).get(5, TimeUnit.SECONDS);
                } catch (Exception e) { throw new AssertionError("Handler holds registry lock", e); }
            }
        };
        try { WebSocket.openReceived(id); }
        finally { pool.shutdownNow(); WebSocket.closeReceived(id, 1000, "done"); }
        check(Log.errors.isEmpty(), "Callback failed: " + Log.errors);
    }

    public static void main(String[] args) throws Exception {
        NativeLookup.register(WebSocketNativeImpl.class, FakeNative.class);
        // Deterministic failure on the old implementation, before the stress test.
        reconnectRetiresOldId();
        handlersDoNotHoldRegistryLock();
        concurrentRouting();
        check(Log.errors.isEmpty(), "Unexpected callback errors: " + Log.errors);
        System.out.println("PASS: reconnect retirement, unlocked handlers, 1600 concurrent constructions and reconnects / 9600 delivered callbacks");
    }
}
