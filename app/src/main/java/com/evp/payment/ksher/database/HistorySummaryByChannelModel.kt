package com.evp.payment.ksher.database


data class HistorySummaryByChannelModel(
    var paymentChannel: String? = "",
    var voidTotalAmount: Int = 0,
    var saleTotalAmount: Int = 0
)