package com.evp.payment.ksher.printing

import com.evp.payment.ksher.utils.messenger.TransactionActionEvent

class PrintResponseEvent : TransactionActionEvent {
    /**
     * Print exception
     */
    var throwable: Throwable? = null

    constructor() {}
    constructor(throwable: Throwable?) {
        this.throwable = throwable
    }
}