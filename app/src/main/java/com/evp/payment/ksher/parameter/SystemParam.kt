package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.LanguageSettingUtil
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.f2prateek.rx.preferences2.Preference
import com.google.common.base.Strings

abstract class SystemParam : AbstractParam() {
    companion object {
        private const val PREFIX = "SystemParam"

        /**
         * 8583 message header
         */
        val iso8583Header = SharedPreferencesUtil.getString("$PREFIX.iso8583Header", "")

        /**
         * Language
         */
        @JvmStatic
        val language = SharedPreferencesUtil.getString("$PREFIX.language", LanguageSettingUtil.ENGLISH)

        /**
         * Main page title
         */
        val mainPageTitle = SharedPreferencesUtil.getString("$PREFIX.mainPageTitle", "")

        /**
         * Whether the transaction needs to input password
         */
        val supervisorPasswordNeeded =
            SharedPreferencesUtil.getBoolean("$PREFIX.supervisorPasswordNeeded", true)

        /**
         * Whether to use mock communication
         */
        val transactionMock = SharedPreferencesUtil.getBoolean("$PREFIX.transactionMock", false)

        /**
         * System trace audit number
         */
        val traceNo = SharedPreferencesUtil.getString("$PREFIX.traceNo", "000001")

        /**
         * Transaction invoice number, increase if transaction increase
         */
        val invoiceNo = SharedPreferencesUtil.getString("$PREFIX.invoiceNo", "000001")

        /**
         * Batch number
         */
        val batchNo = SharedPreferencesUtil.getString("$PREFIX.batchNo", "000001")

        /**
         * Max number of transaction records
         */
        @JvmStatic
        val systemMaxTransNumberDefault = SharedPreferencesUtil.getString("$PREFIX.systemMaxTransNumberDefault", "500")

        @JvmStatic
        val systemPrintGrayDefault = SharedPreferencesUtil.getString("$PREFIX.systemPrintGrayDefault", "500")


        /**
         * Whether to enable keypad tone
         */
        val keypadToneEnabled = SharedPreferencesUtil.getBoolean("$PREFIX.keypadToneEnabled", true)

        /**
         * Whether to use front camera
         */
        val frontCameraScanEnabled = SharedPreferencesUtil.getBoolean("$PREFIX.frontCameraScanEnabled", false)

        @JvmStatic
        val connectionTimeout = SharedPreferencesUtil.getString("$PREFIX.connectionTimeout", "60")

        @JvmStatic
        val transactionTimeout = SharedPreferencesUtil.getString("$PREFIX.transactionTimeout", "60")

        @JvmStatic
        val configVersion = SharedPreferencesUtil.getString("$PREFIX.configVersion", "")

        @JvmStatic
        val appIdOnline = SharedPreferencesUtil.getString("$PREFIX.appIdOnline", "")

        @JvmStatic
        val tokenOnline = SharedPreferencesUtil.getString("$PREFIX.tokenOnline", "")

        @JvmStatic
        val appIdOffline = SharedPreferencesUtil.getString("$PREFIX.appIdOffline", "")

        @JvmStatic
        val tokenOffline = SharedPreferencesUtil.getString("$PREFIX.tokenOffline", "")

        @JvmStatic
        val isInvokeFlag = SharedPreferencesUtil.getBoolean("$PREFIX.isInvokeFlag", false)

        @JvmStatic
        val configName = SharedPreferencesUtil.getString("$PREFIX.configName", "")

        @JvmStatic
        val resourceName = SharedPreferencesUtil.getString("$PREFIX.resourceName", "")

        @JvmStatic
        val paymentDomain = SharedPreferencesUtil.getString("$PREFIX.paymentDomain", "")

        @JvmStatic
        val gateWayDomain = SharedPreferencesUtil.getString("$PREFIX.gateWayDomain", "")

        @JvmStatic
        val publicKey = SharedPreferencesUtil.getString("$PREFIX.publicKey", "")

        @JvmStatic
        val trueMoneyQRTime = SharedPreferencesUtil.getString("$PREFIX.trueMoneyQRTime", "60")

        @JvmStatic
        val prpmptPayQRTime = SharedPreferencesUtil.getString("$PREFIX.prpmptPayQRTime", "60")

        @JvmStatic
        val communicationMode = SharedPreferencesUtil.getString("$PREFIX.communicationMode", "MOBILE")
        /**
         * Increase invoice number
         */
        fun incInvoiceNo() {
            incNumber(invoiceNo)
        }

        /**
         * Increase trace number
         */
        fun incTraceNo() {
            incNumber(traceNo)
        }

        /**
         * Increase batch number
         */
        fun incBatchNo() {
            incNumber(batchNo)
        }

        private fun incNumber(preference: Preference<String>) {
            var number = 0
            try {
                number = preference.get()!!.toInt()
            } catch (ignore: NumberFormatException) {
            }
            ++number
            if (number > 999999) {
                number = 1
            }
            preference.set(Strings.padStart(number.toString(), 6, '0'))
        }
    }

}