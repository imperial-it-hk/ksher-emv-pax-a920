package com.evp.payment.ksher.printing.generator

import android.graphics.Bitmap

class PrintingMarkups {
    var slipType: Int
    var text: String? = null
    var bitmap: Bitmap? = null

    constructor(slipType: Int, text: String?) {
        this.slipType = slipType
        this.text = text
    }

    constructor(slipType: Int, bitmap: Bitmap?) {
        this.slipType = slipType
        this.bitmap = bitmap
    }

    interface SlipType {
        companion object {
            /**
             * Only print on merchant slip
             */
            const val MER = 0

            /**
             * Only print on customer slip
             */
            const val CUS = 1

            /**
             * Print on all slip
             */
            const val ALL = 2
        }
    }
}