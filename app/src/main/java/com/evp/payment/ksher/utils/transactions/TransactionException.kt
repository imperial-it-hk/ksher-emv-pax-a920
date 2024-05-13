package com.evp.payment.ksher.utils.transactions

/**
 * Transaction processing exception
 */
class TransactionException(var code: Int, msg: String?) : Exception(msg)