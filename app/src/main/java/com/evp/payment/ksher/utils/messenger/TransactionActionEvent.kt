package com.evp.payment.ksher.utils.messenger

import java.io.Serializable

abstract class TransactionActionEvent : Serializable {
    var title: String? = null
}