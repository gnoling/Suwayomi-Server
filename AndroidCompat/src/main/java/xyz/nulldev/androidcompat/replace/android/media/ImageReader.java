package xyz.nulldev.androidcompat.replace.android.media;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.os.Handler;
import xyz.nulldev.androidcompat.replace.android.view.Surface;

/**
 * Stand-in for android.media.ImageReader.
 * <p>
 * On Android, extensions render a WebView into a virtual display backed by an ImageReader so the
 * page behaves as if it were on screen. KCEF already renders offscreen on its own, so no frames are
 * ever delivered here.
 */
public class ImageReader implements AutoCloseable {
    public interface OnImageAvailableListener {
        void onImageAvailable(ImageReader reader);
    }

    private final int width;
    private final int height;
    private final int format;
    private final int maxImages;
    private final Surface surface = new Surface();

    private ImageReader(int width, int height, int format, int maxImages) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.maxImages = maxImages;
    }

    public static ImageReader newInstance(int width, int height, int format, int maxImages) {
        return new ImageReader(width, height, format, maxImages);
    }

    public static ImageReader newInstance(int width, int height, int format, int maxImages, long usage) {
        return new ImageReader(width, height, format, maxImages);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getImageFormat() {
        return format;
    }

    public int getMaxImages() {
        return maxImages;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setOnImageAvailableListener(OnImageAvailableListener listener, Handler handler) {
        // Frames are never produced, see class documentation
    }

    public Image acquireLatestImage() {
        return null;
    }

    public Image acquireNextImage() {
        return null;
    }

    @Override
    public void close() {
        surface.release();
    }
}
