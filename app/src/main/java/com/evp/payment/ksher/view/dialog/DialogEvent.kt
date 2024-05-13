package com.evp.payment.ksher.view.dialog

interface DialogEvent {
    companion object {
        const val CANCEL = 0
        const val CONFIRM = 1
        const val TIMEOUT = 2
    }
}