package xyz.nulldev.androidcompat.replace.android.view;

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

/**
 * Plain-Java stand-in for {@code android.view.MotionEvent}, whose android.jar stub throws from
 * everything. Extensions' references to it (and its PointerCoords/PointerProperties) are
 * rewritten to this class by BytecodeEditor, so they can synthesize touches and dispatch them
 * to a WebView.
 */
public final class MotionEvent {
    public static final int ACTION_MASK = 0xff;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_CANCEL = 3;
    public static final int ACTION_OUTSIDE = 4;
    public static final int ACTION_POINTER_DOWN = 5;
    public static final int ACTION_POINTER_UP = 6;
    public static final int ACTION_HOVER_MOVE = 7;
    public static final int ACTION_SCROLL = 8;
    public static final int ACTION_HOVER_ENTER = 9;
    public static final int ACTION_HOVER_EXIT = 10;
    public static final int ACTION_BUTTON_PRESS = 11;
    public static final int ACTION_BUTTON_RELEASE = 12;
    public static final int ACTION_POINTER_INDEX_MASK = 0xff00;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;

    public static final int TOOL_TYPE_UNKNOWN = 0;
    public static final int TOOL_TYPE_FINGER = 1;
    public static final int TOOL_TYPE_STYLUS = 2;
    public static final int TOOL_TYPE_MOUSE = 3;
    public static final int TOOL_TYPE_ERASER = 4;

    public static final int INVALID_POINTER_ID = -1;

    public static final class PointerCoords {
        public float x;
        public float y;
        public float pressure;
        public float size;
        public float touchMajor;
        public float touchMinor;
        public float toolMajor;
        public float toolMinor;
        public float orientation;
        public float relativeX;
        public float relativeY;

        public PointerCoords() {
        }

        public PointerCoords(PointerCoords other) {
            copyFrom(other);
        }

        public void clear() {
            x = y = pressure = size = 0f;
            touchMajor = touchMinor = toolMajor = toolMinor = orientation = 0f;
            relativeX = relativeY = 0f;
        }

        public void copyFrom(PointerCoords other) {
            x = other.x;
            y = other.y;
            pressure = other.pressure;
            size = other.size;
            touchMajor = other.touchMajor;
            touchMinor = other.touchMinor;
            toolMajor = other.toolMajor;
            toolMinor = other.toolMinor;
            orientation = other.orientation;
            relativeX = other.relativeX;
            relativeY = other.relativeY;
        }
    }

    public static final class PointerProperties {
        public int id;
        public int toolType;

        public PointerProperties() {
            clear();
        }

        public PointerProperties(PointerProperties other) {
            copyFrom(other);
        }

        public void clear() {
            id = INVALID_POINTER_ID;
            toolType = TOOL_TYPE_UNKNOWN;
        }

        public void copyFrom(PointerProperties other) {
            id = other.id;
            toolType = other.toolType;
        }
    }

    private long downTime;
    private long eventTime;
    private int action;
    private PointerProperties[] properties;
    private PointerCoords[] coords;
    private int metaState;
    private int buttonState;
    private float xPrecision;
    private float yPrecision;
    private int deviceId;
    private int edgeFlags;
    private int source;
    private int flags;

    private MotionEvent() {
    }

    public static MotionEvent obtain(
            long downTime,
            long eventTime,
            int action,
            int pointerCount,
            PointerProperties[] pointerProperties,
            PointerCoords[] pointerCoords,
            int metaState,
            int buttonState,
            float xPrecision,
            float yPrecision,
            int deviceId,
            int edgeFlags,
            int source,
            int flags) {
        MotionEvent ev = new MotionEvent();
        ev.downTime = downTime;
        ev.eventTime = eventTime;
        ev.action = action;
        ev.properties = new PointerProperties[pointerCount];
        ev.coords = new PointerCoords[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            ev.properties[i] = new PointerProperties(pointerProperties[i]);
            ev.coords[i] = new PointerCoords(pointerCoords[i]);
        }
        ev.metaState = metaState;
        ev.buttonState = buttonState;
        ev.xPrecision = xPrecision;
        ev.yPrecision = yPrecision;
        ev.deviceId = deviceId;
        ev.edgeFlags = edgeFlags;
        ev.source = source;
        ev.flags = flags;
        return ev;
    }

    public static MotionEvent obtain(
            long downTime,
            long eventTime,
            int action,
            int pointerCount,
            PointerProperties[] pointerProperties,
            PointerCoords[] pointerCoords,
            int metaState,
            int buttonState,
            float xPrecision,
            float yPrecision,
            int deviceId,
            int edgeFlags,
            int source,
            int displayId,
            int flags) {
        return obtain(downTime, eventTime, action, pointerCount, pointerProperties, pointerCoords,
                metaState, buttonState, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);
    }

    public static MotionEvent obtain(
            long downTime,
            long eventTime,
            int action,
            float x,
            float y,
            float pressure,
            float size,
            int metaState,
            float xPrecision,
            float yPrecision,
            int deviceId,
            int edgeFlags) {
        PointerProperties props = new PointerProperties();
        props.id = 0;
        props.toolType = TOOL_TYPE_FINGER;
        PointerCoords c = new PointerCoords();
        c.x = x;
        c.y = y;
        c.pressure = pressure;
        c.size = size;
        return obtain(downTime, eventTime, action, 1, new PointerProperties[] {props}, new PointerCoords[] {c},
                metaState, 0, xPrecision, yPrecision, deviceId, edgeFlags, 0, 0);
    }

    public static MotionEvent obtain(long downTime, long eventTime, int action, float x, float y, int metaState) {
        return obtain(downTime, eventTime, action, x, y, 1f, 1f, metaState, 1f, 1f, 0, 0);
    }

    public static MotionEvent obtain(MotionEvent other) {
        return obtain(other.downTime, other.eventTime, other.action, other.getPointerCount(), other.properties,
                other.coords, other.metaState, other.buttonState, other.xPrecision, other.yPrecision,
                other.deviceId, other.edgeFlags, other.source, other.flags);
    }

    public void recycle() {
    }

    public int getAction() {
        return action;
    }

    public int getActionMasked() {
        return action & ACTION_MASK;
    }

    public int getActionIndex() {
        return (action & ACTION_POINTER_INDEX_MASK) >> ACTION_POINTER_INDEX_SHIFT;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public long getDownTime() {
        return downTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public int getPointerCount() {
        return coords.length;
    }

    public int getPointerId(int pointerIndex) {
        return properties[pointerIndex].id;
    }

    public int findPointerIndex(int pointerId) {
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].id == pointerId) {
                return i;
            }
        }
        return -1;
    }

    public int getToolType(int pointerIndex) {
        return properties[pointerIndex].toolType;
    }

    public float getX() {
        return getX(0);
    }

    public float getY() {
        return getY(0);
    }

    public float getX(int pointerIndex) {
        return coords[pointerIndex].x;
    }

    public float getY(int pointerIndex) {
        return coords[pointerIndex].y;
    }

    public float getRawX() {
        return getX();
    }

    public float getRawY() {
        return getY();
    }

    public float getPressure() {
        return getPressure(0);
    }

    public float getPressure(int pointerIndex) {
        return coords[pointerIndex].pressure;
    }

    public float getSize() {
        return getSize(0);
    }

    public float getSize(int pointerIndex) {
        return coords[pointerIndex].size;
    }

    public float getTouchMajor() {
        return getTouchMajor(0);
    }

    public float getTouchMajor(int pointerIndex) {
        return coords[pointerIndex].touchMajor;
    }

    public float getTouchMinor() {
        return getTouchMinor(0);
    }

    public float getTouchMinor(int pointerIndex) {
        return coords[pointerIndex].touchMinor;
    }

    public float getOrientation() {
        return getOrientation(0);
    }

    public float getOrientation(int pointerIndex) {
        return coords[pointerIndex].orientation;
    }

    public void getPointerCoords(int pointerIndex, PointerCoords outPointerCoords) {
        outPointerCoords.copyFrom(coords[pointerIndex]);
    }

    public void getPointerProperties(int pointerIndex, PointerProperties outPointerProperties) {
        outPointerProperties.copyFrom(properties[pointerIndex]);
    }

    public int getMetaState() {
        return metaState;
    }

    public int getButtonState() {
        return buttonState;
    }

    public float getXPrecision() {
        return xPrecision;
    }

    public float getYPrecision() {
        return yPrecision;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getEdgeFlags() {
        return edgeFlags;
    }

    public int getSource() {
        return source;
    }

    public int getFlags() {
        return flags;
    }

    public void setLocation(float x, float y) {
        offsetLocation(x - getX(), y - getY());
    }

    public void offsetLocation(float deltaX, float deltaY) {
        for (PointerCoords c : coords) {
            c.x += deltaX;
            c.y += deltaY;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MotionEvent { action=").append(action);
        for (int i = 0; i < coords.length; i++) {
            sb.append(", id[").append(i).append("]=").append(properties[i].id)
                    .append(", x[").append(i).append("]=").append(coords[i].x)
                    .append(", y[").append(i).append("]=").append(coords[i].y);
        }
        return sb.append(", eventTime=").append(eventTime).append(" }").toString();
    }
}
