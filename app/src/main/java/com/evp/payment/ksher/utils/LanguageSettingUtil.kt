package com.evp.payment.ksher.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.*

class LanguageSettingUtil(private val context: Context) {
    fun setLanguage(language: String?) {
//        when (language) {
//            ENGLISH -> setLocal(Locale.ENGLISH)
//            THAILAND -> setLocal(Locale("th", "TH"))
//            else -> {
//            }
//        }
    }

    private fun setLocal(locale: Locale) {
        val resources = context.resources
        val dm = resources.displayMetrics
        val config = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            config.setLocale(locale)
        }
        resources.updateConfiguration(config, dm)
    }

    companion object {
        const val ENGLISH = "en"
        const val THAILAND = "th"
    }
}