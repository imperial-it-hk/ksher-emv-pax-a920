package com.evp.payment.ksher.printing.generator

import android.graphics.Bitmap
import com.evp.payment.ksher.parameter.PrintParam
import com.evp.payment.ksher.utils.BytesUtil
import com.google.common.base.Strings

class IsoLogGenerator : BaseReceiptGenerator {
    private var isoMap: Map<String, ByteArray>? = null
    private var additional: List<String>? = null
    private var sendData: ByteArray? = null
    private var receiveData: ByteArray? = null

    constructor(isoMap: Map<String, ByteArray>?, sendData: ByteArray?) {
        this.isoMap = isoMap
        this.sendData = sendData
    }

    constructor(receiveData: ByteArray?, isoMap: Map<String, ByteArray>?) {
        this.receiveData = receiveData
        this.isoMap = isoMap
    }

    constructor(additional: List<String>?) {
        this.additional = additional
    }

    override fun generate(): Bitmap {
        if (additional != null) {
            for (str in additional!!) {
                page.addLine().addUnit(str, PrintFontSize.FONT_NORMAL)
            }
        }
        if (receiveData != null) {
            page.addLine()
                .addUnit(BytesUtil.byteArray2HexString(receiveData), PrintFontSize.FONT_NORMAL)
        }
        if (isoMap != null) {
//            val entrySet = isoMap!!.entries
//            for ((key, value) in entrySet) {
//                when (key) {
//                    "h" -> {
//                        // Not print message header
//                    }
//                    "m" -> {
//                        page.addLine().addUnit("MSG TYPE: " + String(value), PrintFontSize.FONT_NORMAL)
//                    }
//                    else -> {
//                        page.addLine().addUnit(
//                            "BIT " + Strings.padStart(key, 2, ' ') + ": " + PackUtil.getPrintBitValue(
//                                key, value
//                            ), PrintFontSize.FONT_NORMAL
//                        )
//                    }
//                }
//            }
        }
        if (sendData != null) {
            page.addLine()
                .addUnit(BytesUtil.byteArray2HexString(sendData), PrintFontSize.FONT_NORMAL)
        }
        feedLine()
        return page.toBitmap(PrintParam.PRINT_WIDTH)
    }

    fun setAdditional(additional: List<String>?): IsoLogGenerator {
        this.additional = additional
        return this
    }
}