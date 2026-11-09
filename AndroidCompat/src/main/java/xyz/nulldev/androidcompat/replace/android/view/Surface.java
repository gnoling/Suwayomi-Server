package xyz.nulldev.androidcompat.replace.android.view;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

/** Stand-in for android.view.Surface. Nothing is ever drawn into it. */
public class Surface {
    private boolean released;

    public boolean isValid() {
        return !released;
    }

    public void release() {
        released = true;
    }
}
