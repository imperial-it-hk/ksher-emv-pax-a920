package com.evp.payment.ksher.database.table

import android.os.Parcelable
import androidx.room.*
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DateUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "Settlement",
    indices = [Index(value = ["batch_no"], unique = true)]
)
data class SettlementDataModel(
    @ColumnInfo(name = "batch_no")
    var batchNo: Long?,

    @ColumnInfo(name = "terminal_id")
    var terminalId: String?,

    @ColumnInfo(name = "merchant_id")
    var merchantId: String?,

    @ColumnInfo(name = "host_name")
    var hostName: String?,

    @ColumnInfo(name = "channel_datas")
    var channelDatas: String?,

    @ColumnInfo(name = "grand_total_data")
    var grandTotalData: String?,

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
    var tmLastInitDateTime: String?

) : Parcelable {
    @Ignore
    constructor() : this(
        0,
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
        fun init(): SettlementDataModel = SettlementDataModel().apply {
            batchNo = SystemParam.batchNo.get()!!.toLong()
            year = DateUtils.getCurrentTime("yyyy")
            date = DateUtils.getCurrentTime("MMdd")
            time = DateUtils.getCurrentTime("HHmmss")
            tmLastInitDateTime = year + date + time
        }
    }
}
