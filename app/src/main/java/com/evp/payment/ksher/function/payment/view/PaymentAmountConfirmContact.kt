package com.evp.payment.ksher.function.payment.view

import com.evp.payment.ksher.database.table.TransDataModel

interface PaymentAmountConfirmContact {
    interface View {
        fun initMenu()
        fun confirm()
        fun cancel()
        fun displayAmount()
        fun onQuickPayError(error: String, transData: TransDataModel)
        fun onTransactionSuccess(transData: TransDataModel)
        fun onTransactionFailure(transData: TransDataModel)
        fun onTransactionTimeout(transData: TransDataModel)
        fun showDialogMessage(transData: TransDataModel, msg: String)
    }

    interface Presenter {
        fun initialTransaction(payChannel: String, payAmount: String, authCode: String)
    }

}