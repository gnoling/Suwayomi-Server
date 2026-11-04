package xyz.nulldev.androidcompat.replace.android.app;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import xyz.nulldev.androidcompat.replace.android.view.Display;

/**
 * Stand-in for android.app.Presentation (a Dialog shown on another display).
 * <p>
 * There is no window system; the content view is just parented to a container so that
 * extensions see it attached (getParent() is non-null) as they would on Android.
 */
public class Presentation {
    private final Context context;
    private final Display display;
    private final ViewGroup contentParent;
    private boolean showing;

    public Presentation(Context outerContext, Display display) {
        this.context = outerContext;
        this.display = display;
        this.contentParent = new ContentFrame(outerContext);
    }

    public Presentation(Context outerContext, Display display, int theme) {
        this(outerContext, display);
    }

    public Context getContext() {
        return context;
    }

    public Display getDisplay() {
        return display;
    }

    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        contentParent.removeAllViews();
        contentParent.addView(view, params);
    }

    public void show() {
        showing = true;
    }

    public boolean isShowing() {
        return showing;
    }

    public void dismiss() {
        // Like Android, the content view stays parented; only the window goes away
        showing = false;
    }

    public void cancel() {
        dismiss();
    }

    private static final class ContentFrame extends ViewGroup {
        ContentFrame(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }
    }
}
