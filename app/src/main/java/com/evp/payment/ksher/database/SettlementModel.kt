package com.evp.payment.ksher.database

import android.os.Parcelable
import androidx.room.*
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.constant.TransStatus
import kotlinx.android.parcel.Parcelize


data class SettlementModel(
    var settlements : ArrayList<SettlementItemModel> = ArrayList()
)

data class SettlementItemModel(
    var paymentChannel: String? = "",
    var saleCount: Int = 0,
    var saleTotalAmount: Int = 0,
    var refundCount: Int = 0,
    var refundTotalAmount: Int = 0

)
