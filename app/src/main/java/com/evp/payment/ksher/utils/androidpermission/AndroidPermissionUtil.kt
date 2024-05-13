package com.evp.payment.ksher.utils.androidpermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject

object AndroidPermissionUtil {
    var permissions = arrayOf(
        Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE
    )
    var overlaySubject: PublishSubject<*>? = null
    var permitSubject: PublishSubject<*>? = null
    fun requestPermissions(context: Context): Completable {
        return requestOverlay(context).andThen(requestOtherPermissions(context))
    }

    private fun requestOverlay(context: Context): Completable {
        if (checkOverlay(context)) {
            return Completable.complete()
        }
        overlaySubject = PublishSubject.create<Any>()
        return Completable.fromAction {
            val intent = Intent(context, AndroidOverlayActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }.andThen((overlaySubject as PublishSubject<*>).ignoreElements())
    }

    private fun requestOtherPermissions(context: Context): Completable {
        if (checkPermissions(context)) {
            return Completable.complete()
        }
        permitSubject = PublishSubject.create<Any>()
        return Completable.fromAction {
            val intent = Intent(context, AndroidPermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }.andThen((permitSubject as PublishSubject<*>).ignoreElements())
    }

    fun checkOverlay(context: Context?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    fun checkPermissions(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        var needRequest = false
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                needRequest = true
            }
        }
        return !needRequest
    }
}