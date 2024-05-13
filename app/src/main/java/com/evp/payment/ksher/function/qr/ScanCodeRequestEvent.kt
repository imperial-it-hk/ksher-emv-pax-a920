package com.evp.payment.ksher.function.qr

import com.evp.payment.ksher.utils.messenger.TransactionActionEvent

class ScanCodeRequestEvent : TransactionActionEvent() {
    var isManualInputEnabled = true
    var isNotifyByEvent = false
}