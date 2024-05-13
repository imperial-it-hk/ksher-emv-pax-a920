package com.evp.payment.ksher.database.table

import android.os.Parcelable
import androidx.room.*
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.database.SuspendedQrData
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.utils.DateUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "SuspendedQr",
    indices = [Index(value = ["trace_no"], unique = true)]
)
data class SuspendedQrDataModel(

    @ColumnInfo(name = "amount")
    var amount: Long? = null,

    @ColumnInfo(name = "trace_no")
    var traceNo: Long?,

    @ColumnInfo(name = "batch_no")
    var batchNo: Long?,

    @ColumnInfo(name = "mch_order_no")
    var mchOrderNo: String?,

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

    /**
     * Date: MMdd
     */
    @ColumnInfo(name = "date")
    var date: String?,

    /**
     * Time: HHmmss
     */
    @ColumnInfo(name = "time")
    var time: String?,

    /**
     * TM table last initialize time
     */
    @ColumnInfo(name = "tm_last_init_date_time")
    var tmLastInitDateTime: String?,

    @ColumnInfo(name = "imgdat")
    var qrCode: String?,

    @ColumnInfo(name = "tmn_expire_time")
    var qrExpireTime: String?,

    @ColumnInfo(name = "status")
    var status: String?

) : SuspendedQrData(), Parcelable {
    @Ignore
    constructor() : this(
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
        ""
    )

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null

    companion object {
        fun init(): SuspendedQrDataModel = SuspendedQrDataModel().apply {
            merchantId = MerchantParam.merchantId.get()
            terminalId = TerminalParam.number.get()
            storeId = MerchantParam.storeId.get()
            traceNo = SystemParam.traceNo.get()!!.toLong()
            batchNo = SystemParam.batchNo.get()!!.toLong()
            year = DateUtils.getCurrentTime("yyyy")
            date = DateUtils.getCurrentTime("MMdd")
            time = DateUtils.getCurrentTime("HHmmss")
            tmLastInitDateTime = year + date + time
            status = "PENDING"

            if (!BuildConfig.DEBUG)
                mchOrderNo = terminalId + year + date + traceNo
            else
                mchOrderNo = terminalId + year + date + time + traceNo
        }
    }

    fun isPrintQrEnabled() = true
}
