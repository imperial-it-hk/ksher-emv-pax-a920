package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.function.BaseApplication

abstract class AbstractParam {
    companion object {
        protected fun getStr(resId: Int): String {
            return BaseApplication.appContext!!.getString(resId)
        }
    }
}