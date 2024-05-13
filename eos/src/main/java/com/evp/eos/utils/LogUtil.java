package com.evp.eos.utils;

import android.util.Log;

public class LogUtil {
    /**
     * 控制android.util.Log.v是否输出
     * Control whether the android.util.Log.v is output
     */
    private static boolean DEBUG_V = false;
    /**
     * 控制android.util.Log.d是否输出
     * Control whether the android.util.Log.d is output
     */
    private static boolean DEBUG_D = false;
    /**
     * 控制android.util.Log.i是否输出
     * Control whether the android.util.Log.i is output
     */
    private static boolean DEBUG_I = false;
    /**
     * 控制android.util.Log.w是否输出
     * Control whether the android.util.Log.w is output
     */
    private static boolean DEBUG_W = false;
    /**
     * 控制android.util.Log.e是否输出
     * Control whether the android.util.Log.e is output
     */
    private static boolean DEBUG_E = false;

    public enum EDebugLevel {
        DEBUG_V,
        DEBUG_D,
        DEBUG_I,
        DEBUG_W,
        DEBUG_E,
    }

    /**
     * 同时控制V/D/I/W/E 5种输出开关
     * Simultaneous control V/D/I/W/E output
     *
     * @param debugFlag 开关, true打开, false关闭
     *                  Switch, true open, false close
     */
    public static void debug(boolean debugFlag) {
        DEBUG_V = debugFlag;
        DEBUG_D = debugFlag;
        DEBUG_I = debugFlag;
        DEBUG_W = debugFlag;
        DEBUG_E = debugFlag;
    }

    /**
     * 分别控制V/D/I/W/E 5种输出开关
     * Each control V/D/I/W/E output
     *
     * @param debugFlag 开关, true打开, false关闭
     *                  Switch, true open, false close
     */
    public static void debug(EDebugLevel debugLevel, boolean debugFlag) {
        if (debugLevel == EDebugLevel.DEBUG_V) {
            DEBUG_V = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_D) {
            DEBUG_D = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_I) {
            DEBUG_I = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_W) {
            DEBUG_W = debugFlag;
        } else if (debugLevel == EDebugLevel.DEBUG_E) {
            DEBUG_E = debugFlag;
        }
    }

    public static void v(String tag, String msg) {
        if (DEBUG_V) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG_D) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG_I) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG_W) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, Throwable e) {
        if (DEBUG_W) {
            Log.w(tag, e);
        }
    }

    public static void w(String tag, String msg, Throwable e) {
        if (DEBUG_W) {
            Log.w(tag, msg, e);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG_E) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, Throwable e) {
        if (DEBUG_E) {
            Log.e(tag, e.getMessage(), e);
        }
    }

    public static void e(String tag, String msg, Throwable e) {
        if (DEBUG_E) {
            Log.e(tag, msg, e);
        }
    }

}
