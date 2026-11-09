package xyz.nulldev.androidcompat.replace.android.media;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

/** Stand-in for android.media.Image. ImageReader never produces one. */
public abstract class Image implements AutoCloseable {
    public abstract int getFormat();

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract long getTimestamp();

    @Override
    public abstract void close();
}
