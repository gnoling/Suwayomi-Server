package xyz.nulldev.androidcompat.replace.android.os;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.os.Handler;
import android.os.Looper;

/**
 * Stand-in for android.os.HandlerThread (stubbed in android.jar), following the AOSP implementation:
 * a thread that runs its own Looper.
 */
public class HandlerThread extends Thread {
    private final int priority;
    private int tid = -1;
    private Looper looper;
    private Handler handler;

    public HandlerThread(String name) {
        this(name, 0);
    }

    public HandlerThread(String name, int priority) {
        super(name);
        this.priority = priority;
        // Don't keep the server alive because an extension forgot to quit its thread
        setDaemon(true);
    }

    protected void onLooperPrepared() {
    }

    @Override
    public void run() {
        tid = (int) threadId();
        Looper.prepare();
        synchronized (this) {
            looper = Looper.myLooper();
            notifyAll();
        }
        onLooperPrepared();
        Looper.loop();
        tid = -1;
    }

    public Looper getLooper() {
        if (!isAlive()) {
            return null;
        }
        boolean wasInterrupted = false;
        synchronized (this) {
            while (isAlive() && looper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    wasInterrupted = true;
                }
            }
        }
        if (wasInterrupted) {
            Thread.currentThread().interrupt();
        }
        return looper;
    }

    public Handler getThreadHandler() {
        if (handler == null) {
            handler = new Handler(getLooper());
        }
        return handler;
    }

    public boolean quit() {
        Looper l = getLooper();
        if (l != null) {
            l.quit();
            return true;
        }
        return false;
    }

    public boolean quitSafely() {
        Looper l = getLooper();
        if (l != null) {
            l.quitSafely();
            return true;
        }
        return false;
    }

    public int getThreadId() {
        return tid;
    }
}
