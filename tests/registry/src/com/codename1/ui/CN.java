package com.codename1.ui;

/** No EDT is started by these background-callback tests. */
public final class CN {
    public static boolean isEdt() { return false; }
    public static void callSerially(Runnable r) { throw new AssertionError("Unexpected EDT dispatch"); }
    public static void invokeAndBlock(Runnable r) { r.run(); }
}
