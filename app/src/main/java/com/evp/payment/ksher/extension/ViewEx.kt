package com.evp.payment.ksher.extension

import android.graphics.Point
import android.graphics.Rect
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.LayoutRes
import com.evp.payment.ksher.extension.Time.defaultInterval
import com.evp.payment.ksher.extension.Time.lastTimeClicked
import timber.log.Timber

fun View?.visible() {
    this?.visibility = View.VISIBLE
}

fun View?.invisible() {
    this?.visibility = View.INVISIBLE
}

fun View?.gone() {
    this?.visibility = View.GONE
}

fun <T : View> T?.handleClickable(block: (T) -> Unit) {
    this?.setOnClickListener {
        Timber.d("handleClickable: Clicked")
        if (SystemClock.elapsedRealtime() - lastTimeClicked > defaultInterval) {
            Timber.d("handleClickable: Successed")
            lastTimeClicked = SystemClock.elapsedRealtime()
            block(it as T)
        } else {
            Timber.d("handleClickable: Failed")
        }
    }
}

fun <T : View> T?.handleClickable(block: (T) -> Unit, interval: Long ) {
    this?.setOnClickListener {
        Timber.d("handleClickable: Clicked")
        if (SystemClock.elapsedRealtime() - lastTimeClicked > interval) {
            Timber.d("handleClickable: Successed")
            lastTimeClicked = SystemClock.elapsedRealtime()
            block(it as T)
        } else {
            Timber.d("handleClickable: Failed")
        }
    }
}

object Time {
    const val defaultInterval = 750
    var lastTimeClicked: Long = 0
}

/**
 * Change this view's visibility to [View.GONE] if [predicate] returns `true`,
 * otherwise defaults to [default].
 */
fun View.goneIf(default: Int = View.VISIBLE, predicate: () -> Boolean) {
    if (predicate.invoke()) {
        this.visibility = View.GONE
    } else {
        this.visibility = default
    }
}

fun View.focusForAccessibility() {
    this.requestFocus()
    this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
}

fun View.isPointInsideViewBounds(point: Point): Boolean = Rect().run {

    this@isPointInsideViewBounds.getDrawingRect(this)

    IntArray(2).also { locationOnScreen ->
        this@isPointInsideViewBounds.getLocationOnScreen(locationOnScreen)
        offset(locationOnScreen[0], locationOnScreen[1])
    }

    contains(point.x, point.y)
}

fun ViewGroup.inflateView(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}

fun ViewGroup.attachView(@LayoutRes layoutId: Int) {
    LayoutInflater.from(context).inflate(layoutId, this, true)
}