package com.evp.payment.ksher.function.qr.view

import com.evp.payment.ksher.database.table.TransDataModel

interface GeneratorQRContact {
    interface View {
        fun initMenu()
        fun confirm()
        fun cancel()
        fun displayAmount()
        fun showQr(qrString: String)
        fun onGenerateQRError(message: String)
        fun onTransactionSuccess(transData: TransDataModel)
        fun onTransactionFailure(transData: TransDataModel)
        fun onTransactionTimeout(transData: TransDataModel)
        fun stopAutoInquiry()
        fun showQrImage64(string: String)
        fun stopCountdown()
        fun showDialogMessage(msg: String)

    }

    interface Presenter {
        fun initialTransaction(payChannel: String, payAmount: String)
        fun inquiryLastTransaction()
        fun inquiryLastTransactionFinal()
        fun isProcessTransactionDone() : Boolean
    }
}