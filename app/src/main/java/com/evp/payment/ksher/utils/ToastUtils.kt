package com.evp.payment.ksher.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.BaseApplication.Companion.appContext
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

object ToastUtils {
    private var oldMsg: String? = null
    private var toast: Toast? = null
    private var oneTime: Long = 0
    private var twoTime: Long = 0
    fun showMessage(message: String) {
        Completable.fromAction {
            val inflate =
                appContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflate.inflate(R.layout.layout_toast, null)
            val textView = view.findViewById<TextView>(R.id.tv_message)
            if (toast == null) {
                textView.text = message
                toast = Toast(appContext)
                toast?.duration = Toast.LENGTH_SHORT
                toast?.setGravity(Gravity.CENTER, 0, 0)
                toast?.view = view
                toast?.show()
                oneTime = System.currentTimeMillis()
            } else {
                twoTime = System.currentTimeMillis()
                if (message == oldMsg) {
                    if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                        toast!!.show()
                    }
                } else {
                    oldMsg = message
                    textView.text = message
                    toast?.view = view
                    toast?.show()
                }
            }
            oneTime = twoTime
        }.subscribeOn(AndroidSchedulers.mainThread()).onErrorComplete().subscribe()
    }
}