package com.evp.payment.ksher.utils.constant

/**
 * Local error code
 */
object LocalErrorCode {
    /**
     * Transaction success
     */
    const val SUCC = 0

    /**
     * Connect timeout
     */
    const val ERR_CONNECT = -1

    /**
     * Send failed
     */
    const val ERR_SEND = -2

    /**
     * Receive failed
     */
    const val RECEIVE_FAIL = -3

    /**
     * Pack failed
     */
    const val ERR_PACK = -4

    /**
     * Unpack failed
     */
    const val ERR_UNPACK = -5

    /**
     * Process code inconsistent
     */
    const val ERR_PROC_CODE = -6

    /**
     * Transaction amount inconsistent
     */
    const val ERR_TRANS_AMT = -7

    /**
     * System trace audit number inconsistent
     */
    const val ERR_TRACE_NO = -8

    /**
     * Terminal id inconsistent
     */
    const val ERR_TERM_ID = -9

    /**
     * Merchant number inconsistent
     */
    const val ERR_MERCH_ID = -10

    /**
     * 39 field response code is failed
     */
    const val ERR_RESPONSE = -11

    /**
     * No transaction records
     */
    const val ERR_NO_TRANS = -12

    /**
     * Transaction aborted
     */
    const val ERR_ABORTED = -13

    /**
     * Need to settlement immediately
     */
    const val ERR_NEED_SETTLE_NOW = -14

    /**
     * Transaction is not supported
     */
    const val ERR_NOT_SUPPORT_TRANS = -15

    /**
     * Print failed
     */
    const val ERR_PRINT = -16
    fun isReceiveFail(errCode: Int): Boolean {
        return errCode == RECEIVE_FAIL || errCode == ERR_UNPACK || errCode == ERR_PROC_CODE || errCode == ERR_TRANS_AMT || errCode == ERR_TRACE_NO || errCode == ERR_TERM_ID || errCode == ERR_MERCH_ID
    }
}