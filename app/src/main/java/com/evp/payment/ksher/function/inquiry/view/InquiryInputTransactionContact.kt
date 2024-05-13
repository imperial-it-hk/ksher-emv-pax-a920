package com.evp.payment.ksher.function.inquiry.view

import com.evp.payment.ksher.database.table.TransDataModel

interface InquiryInputTransactionContact {
    interface View {
        fun onTransactionSuccess(transData: TransDataModel)
        fun onTransactionFailure(transData: TransDataModel)
        fun onTransactionTimeout(transData: TransDataModel)
        fun onTransactionEmpty()
        fun showDialogMessage(title: String)
    }

    interface Presenter {
        fun inquiryAnyTransaction(tracNo: String)
        fun isProcessTransactionDone() : Boolean
    }

}