package com.evp.payment.ksher.invoke

import android.os.Parcelable
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.database.table.TransDataModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvokeResponseModel(
    var header: InvokeResponseHeaderData? = null,
    var data: InvokeResponseDescriptionData? = null
) : Parcelable

@Parcelize
data class InvokeResponseHeaderData(
    var version: String? = BuildConfig.VERSION_NAME
) : Parcelable


@Parcelize
data class InvokeResponseDescriptionData(
    var respcode: String? = "",
    var message: String? = "",
    var description: TransDataModel? = null
) : Parcelable
