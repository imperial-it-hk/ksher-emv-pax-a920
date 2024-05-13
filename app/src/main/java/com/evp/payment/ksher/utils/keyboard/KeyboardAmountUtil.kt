package com.evp.payment.ksher.utils.keyboard

import android.view.View
import android.view.ViewGroup
import com.evp.payment.ksher.R
import com.evp.payment.ksher.utils.StringUtils.toDisplayAmount

class KeyboardAmountUtil : View.OnClickListener {
    private var amountLong = ""
    private val decimal = 2

    //    private boolean keypadToneEnabled = SystemParam.keypadToneEnabled.get();
    private var listener: KeyboardAmountListener? = null
    private var keypadEnabled = true
    fun init(keyContainer: ViewGroup, listener: KeyboardAmountListener?) {
        // Bind listener
        var i = 0
        val l = keyContainer.childCount
        while (i < l) {
            keyContainer.getChildAt(i).setOnClickListener(this)
            i++
        }
        this.listener = listener
        updateAmount()
    }

    fun setKeypadEnabled(keypadEnabled: Boolean) {
        this.keypadEnabled = keypadEnabled
    }

    private fun keyEnter(`val`: String) {
        var amtStr = amountLong + `val`

        // The input does not begin with 0
//        if (amtStr.toLong() == 0L) return

        // The maximum length does not exceed 9
        if (amtStr.length >= 9) {
            amtStr = amtStr.substring(0, 9)
        }
        amountLong = amtStr
        updateAmount()
    }

    private fun clear() {
        amountLong = ""
        updateAmount()
    }

    private fun delete() {
        if (amountLong.isEmpty()) return
        amountLong = amountLong.substring(0, amountLong.length - 1)
        updateAmount()
    }

    private fun updateAmount() {
        if (listener != null) {
            listener?.updateAmount(toDisplayAmount(amountLong, decimal))
        }
    }

    override fun onClick(v: View) {
        if (!keypadEnabled) return
        when (v.id) {
            R.id.key_1 -> keyEnter("1")
            R.id.key_2 -> keyEnter("2")
            R.id.key_3 -> keyEnter("3")
            R.id.key_4 -> keyEnter("4")
            R.id.key_5 -> keyEnter("5")
            R.id.key_6 -> keyEnter("6")
            R.id.key_7 -> keyEnter("7")
            R.id.key_8 -> keyEnter("8")
            R.id.key_9 -> keyEnter("9")
            R.id.key_0 -> keyEnter("0")
            R.id.key_c -> clear()
            R.id.key_del -> delete()
            R.id.key_enter -> if (listener != null) {
                listener?.confirm()
            }
        }
    }

    interface KeyboardAmountListener {
        fun updateAmount(displayAmount: String?)
        fun confirm()
    }
}