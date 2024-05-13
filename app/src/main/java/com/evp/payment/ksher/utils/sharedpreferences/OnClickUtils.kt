package com.evp.payment.ksher.utils.sharedpreferences

import android.os.SystemClock
import java.lang.ref.WeakReference

object OnClickUtils {
    private val ONCLICK_TIME =
        LongArray(2) //  Array length 2 means only record double click
    private const val INTERVAL_TIME = 500 // 限定Limited interval
    private var shakeObjectRef: WeakReference<Any>? = null
    private var SHAKE_CLICK_TIME: Long = 0
    private const val SHAKE_INTERVAL_TIME = 500

    /**
     * Whether a double-click operation was performed in a short period of time
     */
    val isOnDoubleClick: Boolean
        get() {
            System.arraycopy(ONCLICK_TIME, 1, ONCLICK_TIME, 0, ONCLICK_TIME.size - 1)
            ONCLICK_TIME[ONCLICK_TIME.size - 1] = SystemClock.uptimeMillis()
            return ONCLICK_TIME[0] >= SystemClock.uptimeMillis() - INTERVAL_TIME
        }

    /**
     * Click anti-shake
     */
    fun isShakeClick(obj: Any): Boolean {
        val curTime = SystemClock.uptimeMillis()
        if (shakeObjectRef == null || shakeObjectRef!!.get() !== obj) {
            // Different objects don't need to be anti-shake
            shakeObjectRef = WeakReference(obj)
            SHAKE_CLICK_TIME = curTime
            return false
        }
        // Same objects anti-shake
        return if (curTime < SHAKE_CLICK_TIME + SHAKE_INTERVAL_TIME) {
            true
        } else {
            SHAKE_CLICK_TIME = curTime
            false
        }
    }
}