package com.evp.payment.ksher.function.qr

import com.evp.payment.ksher.utils.messenger.TransactionActionEvent

class ScanCodeResponseEvent : TransactionActionEvent {
    var code: String? = null
    var isManualInput = false

    constructor(code: String?) {
        this.code = code
    }

    constructor(manualInput: Boolean) {
        isManualInput = manualInput
    }
}