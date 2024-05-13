package com.evp.payment.ksher.function.voided.view

import com.evp.payment.ksher.database.table.TransDataModel

interface InputTransactionContact {
    interface View {
        fun scanQR()
        fun onVoidFail(message: String, transData: TransDataModel)
        fun gotoPrinting(transData: TransDataModel)
        fun reInputOrFail(transactionNotFound: String)
        fun backToHome()
        fun showDialogConfirm(transData: TransDataModel)
    }

    interface Presenter {
        fun initialTransaction(orderId: String)
        fun validateOrigTransData(transData: TransDataModel)
    }
}