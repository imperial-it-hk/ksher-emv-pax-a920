package com.evp.payment.ksher.function.settings.view

import android.app.Activity

interface SettingContact {
    interface View {
        fun initMenu()
        fun passwordFunction()
        fun configFunction()
        fun uploadParamFunction()
        fun languageFunction()
    }
}