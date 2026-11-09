package xyz.nulldev.androidcompat.replace.android.hardware.display;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import xyz.nulldev.androidcompat.replace.android.view.Display;
import xyz.nulldev.androidcompat.replace.android.view.Surface;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stand-in for android.hardware.display.DisplayManager, returned by
 * Context.getSystemService(Context.DISPLAY_SERVICE). Virtual displays are bookkeeping only.
 */
public class DisplayManager {
    public static final DisplayManager INSTANCE = new DisplayManager();

    private final Display defaultDisplay = new Display(Display.DEFAULT_DISPLAY, "Built-in Screen");
    private final AtomicInteger nextDisplayId = new AtomicInteger(Display.DEFAULT_DISPLAY + 1);

    private DisplayManager() {
    }

    public Display getDisplay(int displayId) {
        return displayId == Display.DEFAULT_DISPLAY ? defaultDisplay : null;
    }

    public Display[] getDisplays() {
        return new Display[] {defaultDisplay};
    }

    public VirtualDisplay createVirtualDisplay(String name, int width, int height, int densityDpi, Surface surface, int flags) {
        return new VirtualDisplay(new Display(nextDisplayId.getAndIncrement(), name), surface);
    }
}
