package com.evp.payment.ksher.function.main

import android.app.Activity
import android.os.Bundle

/**
 * Used to clear the activity stack
 */
class ExitAppActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}