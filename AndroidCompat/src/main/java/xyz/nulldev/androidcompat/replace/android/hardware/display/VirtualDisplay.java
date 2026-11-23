package xyz.nulldev.androidcompat.replace.android.hardware.display;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import xyz.nulldev.androidcompat.replace.android.view.Display;
import xyz.nulldev.androidcompat.replace.android.view.Surface;

/** Stand-in for android.hardware.display.VirtualDisplay. */
public class VirtualDisplay {
    private final Display display;
    private Surface surface;

    VirtualDisplay(Display display, Surface surface) {
        this.display = display;
        this.surface = surface;
    }

    public Display getDisplay() {
        return display;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public void resize(int width, int height, int densityDpi) {
    }

    public void release() {
    }
}
