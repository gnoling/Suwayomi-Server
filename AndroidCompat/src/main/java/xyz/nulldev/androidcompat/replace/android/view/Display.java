package xyz.nulldev.androidcompat.replace.android.view;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.util.DisplayMetrics;
import xyz.nulldev.androidcompat.res.CompatResources;

/** Stand-in for android.view.Display, describing the headless WebView viewport. */
public class Display {
    public static final int DEFAULT_DISPLAY = 0;

    private final int displayId;
    private final String name;

    public Display(int displayId, String name) {
        this.displayId = displayId;
        this.name = name;
    }

    public int getDisplayId() {
        return displayId;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return true;
    }

    public int getWidth() {
        return CompatResources.WIDTH_PIXELS;
    }

    public int getHeight() {
        return CompatResources.HEIGHT_PIXELS;
    }

    public int getRotation() {
        return 0;
    }

    public float getRefreshRate() {
        return 60f;
    }

    public void getMetrics(DisplayMetrics outMetrics) {
        DisplayMetrics m = CompatResources.INSTANCE.getDisplayMetrics();
        outMetrics.widthPixels = m.widthPixels;
        outMetrics.heightPixels = m.heightPixels;
        outMetrics.density = m.density;
        outMetrics.densityDpi = m.densityDpi;
        outMetrics.scaledDensity = m.scaledDensity;
        outMetrics.xdpi = m.xdpi;
        outMetrics.ydpi = m.ydpi;
    }

    public void getRealMetrics(DisplayMetrics outMetrics) {
        getMetrics(outMetrics);
    }
}
