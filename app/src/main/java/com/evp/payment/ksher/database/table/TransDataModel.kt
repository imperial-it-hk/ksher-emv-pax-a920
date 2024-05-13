package com.evp.payment.ksher.database.table

import android.os.Parcelable
import androidx.room.*
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.database.HistoryData
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.constant.TransStatus
import com.evp.payment.ksher.utils.transactions.ETransType
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "Transaction",
    indices = [Index(value = ["trace_no"], unique = true)]
)
data class TransDataModel(

    @ColumnInfo(name = "trans_type", index = true)
    var transType: String?,

    @ColumnInfo(name = "orig_trans_type")
    var origTransType: String?,

    @ColumnInfo(name = "trans_status", defaultValue = TransStatus.NORMAL)
    var transStatus: String?,

    @ColumnInfo(name = "currencyConvert")
    var currencyConvert: String?,

    @ColumnInfo(name = "amount")
    var amount: Long? = null,

    @ColumnInfo(name = "amountConvert")
    var amountConvert: Long? = null,

    @ColumnInfo(name = "exchangeRate")
    var exchangeRate: String? = "",

    @ColumnInfo(name = "invoice_no")
    var invoiceNo: String? = "",

    @ColumnInfo(name = "trace_no")
    var traceNo: Long?,

    @ColumnInfo(name = "orig_trace_no")
    var origTraceNo: Long?,

    @ColumnInfo(name = "batch_no")
    var batchNo: Long?,

    @ColumnInfo(name = "orig_batch_no")
    var origBatchNo: Long?,

    @ColumnInfo(name = "refer_no")
    var referNo: String?,

    @ColumnInfo(name = "orig_refer_no")
    var origReferNo: String?,

    @ColumnInfo(name = "auth_code")
    var authCode: String?,

    @ColumnInfo(name = "orig_auth_code")
    var origAuthCode: String?,

    @ColumnInfo(name = "mch_order_no")
    var mchOrderNo: String?,

    @ColumnInfo(name = "mch_refund_no")
    var mchRefundNo: String?,

    @ColumnInfo(name = "transaction_id")
    var transactionId: String?,

    @ColumnInfo(name = "payment_channel")
    var paymentChannel: String?,

    @ColumnInfo(name = "terminal_id")
    var terminalId: String?,

    @ColumnInfo(name = "merchant_id")
    var merchantId: String?,

    @ColumnInfo(name = "store_id")
    var storeId: String?,

    @ColumnInfo(name = "appid")
    var appid: String?,

    /**
     * Original year: yyyy
     */
    @ColumnInfo(name = "year")
    var year: String?,

    @ColumnInfo(name = "orig_year")
    var origYear: String?,

    /**
     * Date: MMdd
     */
    @ColumnInfo(name = "date")
    var date: String?,

    @ColumnInfo(name = "orig_date")
    var origDate: String?,
    /**
     * Time: HHmmss
     */
    @ColumnInfo(name = "time")
    var time: String?,

    @ColumnInfo(name = "orig_time")
    var origTime: String?,

    /**
     * Settlement batch statistics
     */
    @ColumnInfo(name = "batch_total_send")
    var batchTotalSend: String?,

    /**
     * TM table last initialize time
     */
    @ColumnInfo(name = "tm_last_init_date_time")
    var tmLastInitDateTime: String?

) : HistoryData(), Parcelable {
    @Ignore
    constructor() : this(
        "",
        "",
        "",
        "",
        0,
        0,
        "",
        "",
        0,
        0,
        0,
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null

    companion object {
        fun init(): TransDataModel = TransDataModel().apply {
            merchantId = MerchantParam.merchantId.get()
            terminalId = TerminalParam.number.get()
            storeId = MerchantParam.storeId.get()
            traceNo = SystemParam.traceNo.get()!!.toLong()
            invoiceNo = SystemParam.invoiceNo.get()
            batchNo = SystemParam.batchNo.get()!!.toLong()
            year = DateUtils.getCurrentTime("yyyy")
            date = DateUtils.getCurrentTime("MMdd")
            time = DateUtils.getCurrentTime("HHmmss")
            tmLastInitDateTime = year + date + time

            val yy = DateUtils.getCurrentTime("yy")
            val trace = SystemParam.traceNo.get()
            mchOrderNo = terminalId + yy + date + trace
        }


        fun copyFromOrigTransData(origTransData: TransDataModel): TransDataModel = init().apply {
            origTransType = origTransData.transType

            appid = origTransData.appid
            amount = origTransData.amount
            amountConvert = origTransData.amountConvert
            currencyConvert = origTransData.currencyConvert
            exchangeRate = origTransData.exchangeRate
            invoiceNo = origTransData.invoiceNo
            paymentChannel = origTransData.paymentChannel

            mchOrderNo = origTransData.mchOrderNo
            transactionId = origTransData.transactionId
            origTraceNo = origTransData.traceNo
            origBatchNo = origTransData.batchNo
            origReferNo = origTransData.referNo
            origAuthCode = origTransData.authCode

            origYear = origTransData.year
            origDate = origTransData.date
            origTime = origTransData.time
        }

        fun initFromSuspendedQr(suspendedModel: SuspendedQrDataModel): TransDataModel =
            TransDataModel().apply {
                merchantId = suspendedModel.merchantId
                terminalId = suspendedModel.terminalId
                storeId = suspendedModel.storeId
                traceNo = suspendedModel.traceNo
                paymentChannel = suspendedModel.paymentChannel
                amount = suspendedModel.amount
                transType = ETransType.SALE.toString()
                batchNo = SystemParam.batchNo.get()!!.toLong()
                year = DateUtils.getCurrentTime("yyyy")
                date = DateUtils.getCurrentTime("MMdd")
                time = DateUtils.getCurrentTime("HHmmss")
                tmLastInitDateTime = year + date + time
                mchOrderNo = suspendedModel.mchOrderNo
            }
    }

    fun isPrintQrEnabled() = true
}
