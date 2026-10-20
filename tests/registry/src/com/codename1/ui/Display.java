package com.codename1.ui;

import java.lang.ref.WeakReference;

/** Headless display adapter used only by the registry regression harness. */
public final class Display {
    private static final Display INSTANCE = new Display();
    public static Display getInstance() { return INSTANCE; }
    public boolean isEdt() { return false; }
    public Thread startThread(Runnable r, String name) { return new Thread(r, name); }
    public Object createSoftWeakRef(Object value) { return new WeakReference<Object>(value); }
    public Object extractHardRef(Object ref) { return ((WeakReference<?>) ref).get(); }
}
