package com.evp.payment.ksher.function

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.evp.payment.ksher.function.BaseApplication.Companion.appContext
import com.evp.payment.ksher.function.main.ExitAppActivity
import java.util.*

object ActivityLifecycleCollector {
    private var activityCounter = 0
    private val activityStack: Stack<Activity>? = Stack()

    /**
     * Whether the app is recycled
     */
    var isIsAppRecycled = true
        private set

    /**
     * Destroyed all activitys
     */
    fun finishAllActivity() {
        // When an application is recycled and recreated, the activityStack is emptied, using this method to ensure that all activities can be destroyed
        val context: Context? = appContext
        val intent = Intent(context, ExitAppActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context!!.startActivity(intent)
    }

    /**
     * Keep the activity at the bottom of the stack and destroy the other activities
     */
    fun finishAllActivityExBottom() {
        if (null != activityStack) {
            while (activityStack.size > 1) {
                val ac = activityStack.pop()
                if (null != ac && !ac.isFinishing) {
                    ac.finish()
                }
            }
        }
    }

    fun recreateAllActivity() {
        for (ac in activityStack!!) {
            ac.recreate()
        }
    }

    /**
     * Gets the activity of the top activity on the stack
     */
    fun peekAlived(): Activity? {
        if (null != activityStack) {
            var activity: Activity?
            do {
                activity = activityStack.peek()
            } while (activity == null || activity.isFinishing)
            return activity
        }
        return null
    }

    /**
     * Whether the App is running in the foreground
     */
    val isAppRunningFront: Boolean
        get() = activityCounter > 0

    /**
     * Whether the App is running in the background
     */
    val isAppRunningBack: Boolean
        get() = activityCounter == 0 && activityStack!!.size > 0

    /**
     * Whether the App is running
     */
    val isAppRunning: Boolean
        get() = activityStack!!.size > 0
    val activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks
        get() = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (null != activityStack && null != activity) {
                    activityStack.add(activity)
                }
            }

            override fun onActivityStarted(activity: Activity) {
                activityCounter++
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityCounter--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (null != activityStack && null != activity) {
                    if (activityStack.contains(activity)) {
                        activityStack.remove(activity)
                    }
                }
            }
        }

    fun setIsAppRecycled(isRecycled: Boolean) {
        isIsAppRecycled = isRecycled
    }
}