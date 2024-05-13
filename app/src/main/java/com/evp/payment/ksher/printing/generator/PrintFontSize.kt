package com.evp.payment.ksher.printing.generator

interface PrintFontSize {
    companion object {
        const val FONT_SUPER_BIG = 50
        const val FONT_BIG = 34

        /**
         * A maximum of 32 words per line
         */
        const val FONT_NORMAL = 25
        const val FONT_SMALL = 22
        const val PADDING_SMALL = 8
    }
}