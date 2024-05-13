package com.evp.payment.ksher.function

import android.app.Application
import com.evp.payment.ksher.parameter.AppStoreParam
import com.evp.payment.ksher.utils.DeviceUtil.restoreDeviceStatus
import com.evp.payment.ksher.utils.Initiator
import com.evp.payment.ksher.utils.device.BatteryLevelPolling
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.eos.utils.LogUtil
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import kotlin.system.exitProcess

@HiltAndroidApp
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        initApplication()
    }

    fun initApplication() {
        LogUtil.debug(true)
        Timber.plant(Timber.DebugTree())

//        // Register activity lifecycle event
        appContext?.registerActivityLifecycleCallbacks(ActivityLifecycleCollector.activityLifecycleCallbacks)

        // Initialize SharePreference
        SharedPreferencesUtil.init(appContext)

        // Initialize PaxStore parameter download
        AppStoreParam.init(appContext)
        // Polling battery level
        BatteryLevelPolling.polling()

        // Initialize device, database
        Initiator.initDevice(appContext)

    }

//    private fun readRawResource(@RawRes res: Int): String? {
//        return appContext?.resources?.let { readStream(it.openRawResource(res)) }
//    }
//
//    private fun readStream(`is`: InputStream): String? {
//        val s: Scanner = Scanner(`is`).useDelimiter("\\A")
//        return if (s.hasNext()) s.next() else ""
//    }

    companion object {
        var appContext: BaseApplication? = null
            private set

        /**
         * Exit application
         */
        fun exitApp() {
            restoreDeviceStatus()
            exitProcess(0)
        }
    }
}