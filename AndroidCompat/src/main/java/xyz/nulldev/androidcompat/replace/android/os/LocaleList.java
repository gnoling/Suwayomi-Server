package xyz.nulldev.androidcompat.replace.android.os;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** Stand-in for android.os.LocaleList (stubbed in android.jar). */
public final class LocaleList {
    private final Locale[] locales;

    public LocaleList(Locale... locales) {
        this.locales = locales.clone();
    }

    public static LocaleList getDefault() {
        return new LocaleList(Locale.getDefault());
    }

    public static LocaleList getAdjustedDefault() {
        return getDefault();
    }

    public static LocaleList getEmptyLocaleList() {
        return new LocaleList();
    }

    public static LocaleList forLanguageTags(String list) {
        if (list == null || list.isEmpty()) {
            return getEmptyLocaleList();
        }
        return new LocaleList(Arrays.stream(list.split(",")).map(Locale::forLanguageTag).toArray(Locale[]::new));
    }

    public Locale get(int index) {
        return index >= 0 && index < locales.length ? locales[index] : null;
    }

    public int size() {
        return locales.length;
    }

    public boolean isEmpty() {
        return locales.length == 0;
    }

    public int indexOf(Locale locale) {
        return Arrays.asList(locales).indexOf(locale);
    }

    public String toLanguageTags() {
        return Arrays.stream(locales).map(Locale::toLanguageTag).collect(Collectors.joining(","));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LocaleList && Arrays.equals(locales, ((LocaleList) other).locales);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(locales);
    }

    @Override
    public String toString() {
        return "[" + toLanguageTags() + "]";
    }
}
