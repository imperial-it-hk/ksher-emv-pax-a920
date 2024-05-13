package com.evp.payment.ksher.utils.constant

/**
 * Transaction status
 */
interface TransStatus {
    companion object {
        /**
         * Normal
         */
        const val NORMAL = "NORMAL"

        /**
         * Already void
         */
        const val VOID = "VOID"

        const val REFUND = "REFUND"
    }
}