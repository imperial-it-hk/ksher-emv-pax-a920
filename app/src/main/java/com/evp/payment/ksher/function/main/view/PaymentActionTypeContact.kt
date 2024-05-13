package com.evp.payment.ksher.function.main.view

import com.evp.payment.ksher.database.table.TransDataModel

interface PaymentActionTypeContact {
    interface View {
        fun lastTransactionPrint()
        fun anyTransactionPrint()
        fun lastSettlementPrint()
        fun lastTransactionInquiry()
        fun suspendedQrInquiry()
        fun anyTransactionInquiry()
        fun summaryReport()
        fun auditReport()
        fun allPaymentTypeReport()
        fun selectPaymentTypeReport()
        fun allPaymentTypeAudit()
        fun selectPaymentTypeAudit()
        fun allPaymentSettlement()
        fun selectPaymentTypeSettlement()

        fun onInquiryTransactionSuccess(transData: TransDataModel, isRePrint: Boolean)
        fun onInquiryTransactionFailure(transData: TransDataModel)
        fun onInquiryTransactionFailure(msg: String)
        fun onInquiryTransactionTimeout(transData: TransDataModel)
        fun onInquiryTransactionEmpty()
        fun gotoPrintLastTran(
            transData: TransDataModel,
            isRePrint: Boolean,
            payTitle: String,
            paySubTitle: String
        )

    }

    interface Presenter {
        fun inquiryLastTransaction()
        fun inquiryTransaction(traceNo: String)
        fun isProcessTransactionDone(): Boolean
        fun printLastTransaction()
    }
}