//package com.evp.payment.ksher.db
//
//import com.evp.payment.ksher.parameter.MerchantParam
//import com.evp.payment.ksher.parameter.MerchantParam.storeId
//import com.evp.payment.ksher.parameter.SystemParam.Companion.batchNo
//import com.evp.payment.ksher.parameter.SystemParam.Companion.invoiceNo
//import com.evp.payment.ksher.parameter.SystemParam.Companion.traceNo
//import com.evp.payment.ksher.parameter.TerminalParam
//import com.evp.payment.ksher.utils.DateUtils.getCurrentTime
//import com.evp.payment.ksher.utils.constant.TransStatus
//import com.evp.payment.ksher.utils.transactions.ETransType
//import io.objectbox.annotation.Entity
//import io.objectbox.annotation.Id
//import io.objectbox.annotation.Index
//import io.objectbox.annotation.Unique
//import java.io.Serializable
//
///**
// * Transaction data
// */
//@Entity
//class TransData : Serializable {
//    @Id
//    var id: Long = 0
//
//    /**
//     * Transaction type
//     */
//    @Index
//    var transType: String? = null
//
//    /**
//     * Original transaction type
//     */
//    var origTransType: String? = null
//
//    /**
//     * 3 field: processing code
//     */
//    var processingCode: String? = null
//
//    /**
//     * Transaction status
//     * [TransStatus]
//     */
//    @Index
//    var transStatus = TransStatus.NORMAL
//
//    /**
//     * Transaction amount
//     */
//    var amount: Long = 0
//
//    /**
//     * 62 filed: invoice number
//     */
//    @Index
//    var invoiceNo: Long = 0
//
//    /**
//     * 11 field: system trace audit number
//     */
//    @Unique
//    @Index
//    var traceNo: Long = 0
//
//    /**
//     * Original system trace audit number
//     */
//    var origTraceNo: Long = 0
//
//    /**
//     * Batch number
//     */
//    var batchNo: Long = 0
//
//    /**
//     * Original batch number
//     */
//    var origBatchNo: Long = 0
//
//    /**
//     * 37 field: reference number
//     */
//    var referNo: String? = null
//
//    /**
//     * Original reference number
//     */
//    var origReferNo: String? = null
//
//    /**
//     * mchOrderNo number
//     */
//    var mchOrderNo: String? = null
//
//    var origMchOrderNo: String? = null
//
//    /**
//     * transactionId
//     */
//    var transactionId: String? = null
//
//    /**
//     * Payment Channel number
//     */
//    var paymentChannel: String? = null
//
//    /**
//     * 38 field: auth code
//     */
//    var authCode: String? = null
//
//    /**
//     * Original auth code
//     */
//    var origAuthCode: String? = null
//
//    /**
//     * Terminal number
//     */
//    var terminalId: String? = null
//
//    /**
//     * Merchant number
//     */
//    var merchantId: String? = null
//
//    /**
//     * Store ID
//     */
//    var storeId: String? = null
//
//    /**
//     * Year: yyyy
//     */
//    var year: String? = null
//
//    /**
//     * Original year: yyyy
//     */
//    var origYear: String? = null
//
//    /**
//     * Date: MMdd
//     */
//    var date: String? = null
//
//    /**
//     * Original date: MMdd
//     */
//    var origDate: String? = null
//
//    /**
//     * Time: HHmmss
//     */
//    var time: String? = null
//
//    /**
//     * Original time: HHmmss
//     */
//    var origTime: String? = null
//
//    /**
//     * Account enter mode
//     */
//    var enterMode = 0
//
//    /**
//     * Response code
//     */
//    var responseCode: String? = null
//
//    /**
//     * Response message
//     */
//    var responseMsg: String? = null
//
//    /**
//     * Payment code
//     */
//    var paymentCode: String? = null
//
//    /**
//     * Value added service(RPC) order number
//     */
//    var vasOrderNo: String? = null
//
//    /**
//     * Request UUID
//     */
//    var requestUUID: String? = null
//
//    /**
//     * Reversal/void code
//     */
//    var rvCode: String? = null
//
//    /**
//     * Send custom table data
//     */
//    var customTableSend: String? = null
//
//    /**
//     * Receive custom table data
//     */
//    var customTableReceive: String? = null
//
//    /**
//     * Dolfin custom print content
//     */
//    var slipPrintingMarkups: String? = null
//
//    /**
//     * Dolfin response error message
//     */
//    var errorMessage: String? = null
//
//    /**
//     * Settlement batch statistics
//     */
//    var batchTotalSend: String? = null
//
//    /**
//     * TM table last initialize time
//     */
//    var tmLastInitDateTime: String? = null
//
//    /**
//     * Whether to print QR/Bar code.
//     */
//    var isPrintQrEnabled = true
//    val transTypeEnum: ETransType
//        get() = ETransType.valueOf(transType!!)
//
//    fun copyFromOrigTransData(origTransData: TransData) {
//        // Original transaction type
//        origTransType = origTransData.transType
//        // Transaction amount
//        amount = origTransData.amount
//        // Batch number, system trace audit number and so on.
//        invoiceNo = origTransData.invoiceNo
//        origTraceNo = origTransData.traceNo
//        origBatchNo = origTransData.batchNo
//        origReferNo = origTransData.referNo
//        origAuthCode = origTransData.authCode
//        origMchOrderNo = origTransData.mchOrderNo
//
//        // 时间
//        // Time
//        origYear = origTransData.year
//        origDate = origTransData.date
//        origTime = origTransData.time
//    }
//
//    companion object {
//        fun init(): TransData {
//            val transData = TransData()
//            transData.merchantId = MerchantParam.number.get()
//            transData.terminalId = TerminalParam.number.get()
//            transData.storeId = storeId.get()
//            transData.traceNo = traceNo.get()!!.toLong()
//            transData.invoiceNo = invoiceNo.get()!!.toLong()
//            transData.batchNo = batchNo.get()!!.toLong()
//            transData.year = getCurrentTime("yyyy")
//            transData.date = getCurrentTime("MMdd")
//            transData.time = getCurrentTime("HHmmss")
//            transData.tmLastInitDateTime = transData.year + transData.date + transData.time
//            return transData
//        }
//    }
//}