package com.evp.payment.ksher.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.os.StatFs
import com.evp.payment.ksher.function.BaseApplication
import com.evp.eos.EosService
import com.evp.eos.device.beeper.BeepFrequenceLevel
import com.evp.eos.utils.LogUtil
import java.text.ParseException
import java.text.SimpleDateFormat

object DeviceUtil {
    const val TAG = "DeviceUtil"
    val neptuneVersion: String
        get() {
            try {
                val packageInfo = BaseApplication.appContext?.packageManager?.getPackageInfo(
                    "com.pax.ipp.neptune",
                    0
                )
                return "NeptuneService_" + packageInfo?.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                LogUtil.e(TAG, "", e)
            }
            return ""
        }

    /**
     * Beep success
     */
    fun beepOk() {
        EosService.getDevice().beeper.beep(BeepFrequenceLevel.LEVEL_3, 100)
            .andThen(
                EosService.getDevice().beeper.beep(
                    BeepFrequenceLevel.LEVEL_4, 100))
            .andThen(
                EosService.getDevice().beeper.beep(
                    BeepFrequenceLevel.LEVEL_5, 100))
            .onErrorComplete().subscribe()
    }

    /**
     * Beep scan code prompt
     */
    fun beepScan() {
        EosService.getDevice().beeper.beep(BeepFrequenceLevel.LEVEL_4, 500).onErrorComplete()
            .subscribe()
    }

    /**
     * Beep failed
     */
    fun beepErr() {
        EosService.getDevice().beeper.beep(BeepFrequenceLevel.LEVEL_6, 200).onErrorComplete()
            .subscribe()
    }

    /**
     * Beep prompt
     */
    fun beepPrompt() {
        EosService.getDevice().beeper.beep(BeepFrequenceLevel.LEVEL_6, 50).onErrorComplete()
            .subscribe()
    }

    /**
     * Set system time
     */
    fun setSystemTime(timestamp: String) {
        if (isValidDate(timestamp)) {
            try {
                EosService.getDevice().utils.setSystemTime(timestamp)
            } catch (ignore: Exception) {
            }
        }
    }

    private fun isValidDate(str: String): Boolean {
        var convertSuccess = true
        val format = SimpleDateFormat("yyMMddHHmmss")
        try {
            format.isLenient = false //Specifies whether or not date/time parsing shall be lenient
            format.parse(str)
        } catch (e: ParseException) {
            convertSuccess = false
        }
        return convertSuccess
    }

    fun setDeviceEnableExit() {
        try {
            EosService.getDevice().utils.enableStatusBar(false)
            EosService.getDevice().utils.showNavigationBar(true)
            EosService.getDevice().utils.enableNavigationBackKey(true)
            EosService.getDevice().utils.enableNavigationHomeKey(false)
            EosService.getDevice().utils.enableNavigationRecentKey(false)
            EosService.getDevice().utils.enablePowerKey(true)
        } catch (e: Exception) {
            LogUtil.e(TAG, e)
        }
    }

    fun setDeviceStatus() {
        try {
            EosService.getDevice().utils.enableStatusBar(false)
            EosService.getDevice().utils.showNavigationBar(false)
            EosService.getDevice().utils.enableNavigationBackKey(false)
            EosService.getDevice().utils.enableNavigationHomeKey(false)
            EosService.getDevice().utils.enableNavigationRecentKey(false)
            EosService.getDevice().utils.enablePowerKey(true)
        } catch (e: Exception) {
            LogUtil.e(TAG, e)
        }
    }

    @JvmStatic
    fun restoreDeviceStatus() {
        try {
            EosService.getDevice().utils.enableStatusBar(true)
            EosService.getDevice().utils.showNavigationBar(true)
            EosService.getDevice().utils.enableNavigationBackKey(true)
            EosService.getDevice().utils.enableNavigationHomeKey(true)
            EosService.getDevice().utils.enableNavigationRecentKey(true)
            EosService.getDevice().utils.enablePowerKey(true)
        } catch (e: Exception) {
            LogUtil.e(TAG, e)
        }
    }

    /**
     * Whether power key is enabled
     */
    fun enablePowerKey(enable: Boolean) {
        try {
            EosService.getDevice().utils.enablePowerKey(enable)
        } catch (ignore: Exception) {
        }
    }

    /**
     * Whether has free space
     */
    fun hasFreeSpace(): Boolean {
        val datapath = Environment.getDataDirectory() //Gets the Android data directory.
        val dataFs =
            StatFs(datapath.path) //Retrieve overall information about the space on a filesystem
        val sizes =
            dataFs.freeBlocks.toLong() * dataFs.blockSize.toLong() //The total number of blocks that are free on the file system, including reserved blocks
        val available =
            sizes / (1024 * 1024) //M            //The size, in bytes, of a block on the file system.
        return available >= 1
    }

    fun getVersionName(context: Context, appName: String): String? {
        val pInfo: PackageInfo
        try {
            pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return appName+" - "+pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }
}