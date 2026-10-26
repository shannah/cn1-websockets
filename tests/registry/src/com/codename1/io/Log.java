package com.codename1.io;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Captures diagnostics without initializing the UI or filesystem logger. */
public final class Log {
    public static final List<String> messages = new CopyOnWriteArrayList<String>();
    public static final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
    public static void p(String message) { messages.add(message); }
    public static void e(Throwable error) { errors.add(error); }
}
