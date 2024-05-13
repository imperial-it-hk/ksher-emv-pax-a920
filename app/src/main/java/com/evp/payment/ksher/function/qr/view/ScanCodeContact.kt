package com.evp.payment.ksher.function.qr.view


interface ScanCodeContact {
    interface View {
        fun onFlashClick()
        fun onClickSwitchCamera()
        fun onCancelClick()
        fun showPaymentBanner()
        fun onBackClick()

    }

}