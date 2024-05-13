package com.evp.payment.ksher.utils.constant

/**
 * Account enter mode
 */
interface EnterMode {
    companion object {
        /**
         * Manual enter
         */
        const val MANUAL = 0

        /**
         * Scan code
         */
        const val QR = 1
    }
}