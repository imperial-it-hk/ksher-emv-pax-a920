package com.evp.payment.ksher.utils

import android.content.Context
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.BaseApplication.Companion.appContext
import com.evp.payment.ksher.utils.androidpermission.AndroidPermissionUtil.requestPermissions
import com.evp.eos.EosService
import com.evp.eos.utils.LogUtil
import io.reactivex.Completable

object Initiator {
    private const val TAG = "Initiator"
    private val context: Context? = appContext
    fun initialize(): Completable {
        // Permission request
        return requestPermissions(context!!) // Initialize device, database
            .doOnComplete {
                if (!initDevice(context)) throw Exception(
                    context.getString(R.string.initialization_failed)
                )
            }
    }

     fun initDevice(context: Context?): Boolean {
        try {
            // Initialize device interface
            EosService.initDevice(context)

        } catch (e: Exception) {
            LogUtil.e(TAG, e)
            return false
        }
        return true
    }
}