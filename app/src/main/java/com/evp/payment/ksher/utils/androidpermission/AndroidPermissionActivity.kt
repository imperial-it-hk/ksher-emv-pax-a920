package com.evp.payment.ksher.utils.androidpermission

import android.app.Activity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.evp.payment.ksher.R

class AndroidPermissionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, AndroidPermissionUtil.permissions, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var allPermitted = true
        for (ret in grantResults) {
            if (ret != 0) {
                allPermitted = false
                break
            }
        }
        finish()
        if (allPermitted) {
            AndroidPermissionUtil.permitSubject?.onComplete()
        } else {
            AndroidPermissionUtil.permitSubject?.onError(AndroidPermissionException(getString(R.string.failed_to_obtain_permissions)))
        }
    }
}