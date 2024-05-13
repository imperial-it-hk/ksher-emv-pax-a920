package com.evp.payment.ksher.utils.constant

/**
 * PasswordType
 */
interface PasswordType {
    companion object {
        const val MERCHANT = "merchant"
        const val VOID = "void"
        const val SETTLEMENT = "settlement"
        const val ADMIN = "admin"
    }
}