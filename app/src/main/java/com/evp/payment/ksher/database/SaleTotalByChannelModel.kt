package com.evp.payment.ksher.database


data class SaleTotalByChannelModel(
    var paymentChannel: String? = "",
    var saleCount: Int = 0,
    var saleTotalAmount: Int = 0
) : HistoryData()