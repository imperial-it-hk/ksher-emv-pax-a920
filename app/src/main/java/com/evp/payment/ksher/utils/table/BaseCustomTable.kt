package com.evp.payment.ksher.utils.table

import com.google.common.base.Strings
import java.io.Serializable

abstract class BaseCustomTable : Serializable {
    companion object {
        const val version = "01"


        @JvmStatic
        fun addLength(inStr: String): String {
            val len = inStr.length / 2
            return Strings.padStart(len.toString(), 4, '0') + inStr
        }
    }
}