package com.evp.payment.ksher.invoke

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvokeModel(
    var header: InvokeHeaderData? = null,
    var data: InvokeData? = null
) : Parcelable

@Parcelize
data class InvokeHeaderData(
    var version: String? = ""
) : Parcelable


@Parcelize
data class InvokeData(
    var transaction_type: String? = "",
    var payment_type: String? = "",
    var media_type: String? = "",
    var amount: String? = "",
    var currency: String? = "",
    var language: String? = ""
) : Parcelable
