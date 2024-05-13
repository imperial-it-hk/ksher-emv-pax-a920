package com.evp.payment.ksher.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.view.WindowManager

object Utils {
    /**
     * Get screen width
     */
    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * Get screen height
     */
    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * Get screen density
     */
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }

    /**
     * Convert the dp to pixels
     */
    fun dp2px(context: Context, dp: Float): Int {
        val scale = getScreenDensity(context)
        return (dp * scale + 0.5).toInt()
    }

    /**
     * Whether the screen is portrait or not
     */
    fun isScreenOrientationPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * Keep screen on
     */
    fun keepScreenLongLight(activity: Activity, isOpenLight: Boolean) {
        val window = activity.window
        if (isOpenLight) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun setSystemLightTime(context: Context, time: Int) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            time * 1000
        )
    }

    /**
     * Set font to default size.
     */
    fun setDefaultFont(context: Context) {
        val res = context.resources
        val config = Configuration()
        config.setToDefaults()
        res.updateConfiguration(config, res.displayMetrics)
    }
}