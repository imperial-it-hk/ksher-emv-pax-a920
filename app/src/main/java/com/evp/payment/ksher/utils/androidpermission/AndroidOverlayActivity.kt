package com.evp.payment.ksher.utils.androidpermission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.evp.payment.ksher.R

class AndroidOverlayActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidPermissionUtil.checkOverlay(this)) {
            finish()
            return
        }
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        finish()
        if (AndroidPermissionUtil.checkOverlay(this)) {
            AndroidPermissionUtil.overlaySubject?.onComplete()
        } else {
            AndroidPermissionUtil.overlaySubject?.onError(AndroidPermissionException(getString(R.string.failed_to_obtain_permissions)))
        }
    }
}