package com.evp.payment.ksher.utils

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.StringRes
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.parameter.SystemParam
import com.f2prateek.rx.preferences2.Preference
import com.google.common.base.Strings
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat


object StringUtils {
    fun validTPDU(tpdu: String): Boolean {
        return !Strings.isNullOrEmpty(tpdu) && tpdu.matches(Regex("^[0-9]{10}$"))
    }

    fun validNII(nii: String): Boolean {
        return !Strings.isNullOrEmpty(nii) && nii.matches(Regex("^[0-9]{3}$"))
    }

    fun validRetryTimes(times: String): Boolean {
        return !Strings.isNullOrEmpty(times) && times.matches(Regex("^[1-3]$"))
    }

    fun validTimeout(timeoutStr: String): Boolean {
        return try {
            val timeout = timeoutStr.toInt()
            timeout >= 1 && timeout <= 99
        } catch (e: Exception) {
            false
        }
    }

    fun validIp(ip: String): Boolean {
        return !Strings.isNullOrEmpty(ip) && ip.matches(Regex("^(([01]{0,1}\\d{0,1}\\d|2[0-4]\\d|25[0-5])\\.){3}([01]{0,1}\\d{0,1}\\d|2[0-4]\\d|25[0-5])$"))
    }

    fun validPort(portStr: String): Boolean {
        return try {
            val port = portStr.toInt()
            port >= 1 && port <= 65535
        } catch (e: Exception) {
            false
        }
    }

    fun validDate(date: String?, format: String?): Boolean {
        return try {
            val dateFormat = SimpleDateFormat(format)
            dateFormat.isLenient = false
            dateFormat.parse(date)
            true
        } catch (var3: Exception) {
            false
        }
    }

    fun validTraceNo(traceNo: String): Boolean {
        var trace: Long = 0
        try {
            trace = traceNo.toLong()
        } catch (ignore: Exception) {
        }
        return trace > 0 && trace <= 999999
    }

    fun validReferenceNo(referenceNo: String): Boolean {
        return !Strings.isNullOrEmpty(referenceNo) && referenceNo.matches(Regex("^[A-Za-z0-9]{12}$"))
    }

    fun validMerchantOrderNo(referenceNo: String): Boolean {
        return !Strings.isNullOrEmpty(referenceNo) && referenceNo.matches(Regex("^[0-9]{26}$"))
    }

    fun validAuthCode(authNo: String): Boolean {
        return !Strings.isNullOrEmpty(authNo) && authNo.matches(Regex("^[A-Za-z0-9]{6}$"))
    }

    fun validTransactionDate(date: String): Boolean {
        return !Strings.isNullOrEmpty(date) && date.length == 4 && validDate(date, "MMdd")
    }

    fun validAmount(amt: String): Boolean {
        var amt = amt
        if (Strings.isNullOrEmpty(amt)) {
            return false
        }
        amt = amt.replace(",", "")
        amt = amt.replace(".", "")
        try {
            val amtLong = amt.toLong()
            return amtLong > 0
        } catch (ignored: Exception) {
        }
        return false
    }

    fun toDisplayAmount(amount: Long): String {
        return formatAmount(",##0", amount.toString(), 2)
    }

    /**
     * Format amount, long -> display; decimal -> display; display -> display
     * eg.RMB 000001234567 -> 12,345.67   123456789->1,234,567.89
     * JPY  000000001234 -> 1,234
     */
    fun toDisplayAmount(amount: String): String {
        return formatAmount(",##0", amount, 2)
    }

    /**
     * Format amount, long -> display; decimal -> display
     * eg.RMB 000001234567 -> 12,345.67   123456789->1,234,567.89  1234.56 -> 1,234.56
     * JPY 000000001234 -> 1,234
     */
    fun toDisplayAmount(amount: String, decimalNum: Int): String {
        return formatAmount(",##0", amount, decimalNum)
    }
    /**
     * Format amount, long -> decimal; display -> decimal
     * eg.RMB 000000001234 -> 12.34   1234->12.34
     * JPY 000000001234 -> 1234
     */
    /**
     * Format amount, long -> decimal; display -> decimal
     * eg.RMB 000000001234 -> 12.34   1234->12.34
     * JPY 000000001234 -> 1234
     */
    @JvmOverloads
    fun toDecimalAmount(amount: String, decimalNum: Int = 2): String {
        return formatAmount("0", amount, decimalNum)
    }

    private fun formatAmount(format: String, amount: String, decimalNum: Int): String {
        var format: String? = format
        var amount = amount
        var decimalNum = decimalNum
        if (!Strings.isNullOrEmpty(amount)) {
            amount = amount.replace(",", "")
        }
        var isLong = false
        var amountDouble = 0.00
        try {
            val amountLong = amount.toLong()
            amountDouble = amountLong / Math.pow(10.0, decimalNum.toDouble())
            isLong = true
        } catch (ignored: Exception) {
        }
        if (!isLong) {
            try {
                amountDouble = amount.toDouble()
            } catch (ignored: Exception) {
            }
        }
        if (decimalNum > 11 || decimalNum < 0) {
            decimalNum = 2
        }
        if (decimalNum > 0) {
            format += Strings.padEnd(".", decimalNum + 1, '0')
        }
        return DecimalFormat(format).format(amountDouble)
    }

    /**
     * double -> long
     * According to the amount of money, not lose Numbers after the decimal point
     * eg.RMB ： 123.4 -> 12340
     */
    fun doubleToLongAmount(amountDouble: Double): Long {
        val decimalNum = 2
        var format: String? = "0"
        if (decimalNum > 0) {
            format += Strings.padEnd(".", decimalNum + 1, '0')
        }
        val doubleAmtStr = DecimalFormat(format).format(amountDouble)
        return toLongAmount(doubleAmtStr)
    }

    /**
     * display|decimal amount convert to double
     */
    fun toDoubleAmount(displayAmount: String): Double {
        val amount = toLongAmount(displayAmount)
        val decimalNum = 2
        return amount / Math.pow(10.0, decimalNum.toDouble())
    }

    /**
     * display|decimal amount convert to long
     */
    fun toLongAmount(displayAmount: String): Long {
        return if (Strings.isNullOrEmpty(displayAmount)) {
            0L
        } else try {
            val longStr = displayAmount.replace("[,|.]".toRegex(), "")
            longStr.toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * display|decimal amount convert to long string
     */
    fun toLongAmountStr(amt: String): String {
        return try {
            toLongAmount(amt).toString()
        } catch (e: Exception) {
            "0"
        }
    }

    /**
     * Convert to BCD amount 000011234567
     */
    fun toBCDAmount(amt: Long): String {
        return Strings.padStart(amt.toString(), 12, '0')
    }

    fun getString(@StringRes resId: Int): String {
        return BaseApplication.appContext!!.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return BaseApplication.appContext!!.getString(resId, formatArgs)
    }

    /**
     * Converts a long trace number to a 6-bit string
     */
    fun formatTraceNo(traceNo: Long): String? {
        return if (traceNo < 1 || traceNo > 999999) {
            null
        } else String.format("%06d", traceNo)
    }

    fun formatTraceNo(traceNo: String): String? {
        return formatTraceNo(traceNo.toLong())
    }

    fun getText(value: List<String?>?): String {
        try {
            for (label in value.orEmpty()) {
                if (SystemParam.language.get().orEmpty()
                        .equals(label.orEmpty().split(":")[0], true)
                ) {
                    return label.orEmpty().split(":")[1]
                }
            }
        }catch (e: Exception){
            return ""
        }
        return ""
    }

//    fun getImageBitmap(imgFileName: String?): Bitmap {
//        return BitmapFactory.decodeFile( BaseApplication.appContext?.resources?.getIdentifier(imgFileName,"raw", BaseApplication.appContext?.packageName).toString())
//        return try {
//            if (imgFileName != null) {
//                BaseApplication.appContext?.resources?.getIdentifier(
//                    imgFileName.replace(
//                        ".png", ""
//                    ), "raw", BaseApplication.appContext?.packageName
//                )!!
//            } else {
//                0
//            }
//        } catch (e: Exception) {
//            0
//        }
//    }

    fun getImage(imgFileName: String?): Bitmap? {
        val imgFile = File(BaseApplication.appContext?.filesDir.toString() + File.separator + "ParamDownload/resource/"+imgFileName)

        return if (imgFile.exists()) {
            BitmapFactory.decodeFile(imgFile.absolutePath)
        }else{
            null
        }
    }

    fun getToken(token: String?): String {
        return token.orEmpty()
    }
}

public fun Preference<String>.decrypt(): String = StringUtils.getToken(this.get())