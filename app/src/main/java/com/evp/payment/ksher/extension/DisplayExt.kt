package com.evp.payment.ksher.extension

fun String.toPaymentChannelDisplay(): String {
    val payChannel = this
    return when {
        "linepay".equals(payChannel, true) -> {
            "Rabbit LINE Pay"
        }
        "promptpay".equals(payChannel, true) -> {
            "PromptPay"
        }
        "alipay".equals(payChannel, true) -> {
            "Alipay"
        }
        "wechat".equals(payChannel, true) -> {
            "WeChat"
        }
        "truemoney".equals(payChannel, true) -> {
            "True Money"
        }
        "airpay".equals(payChannel, true) -> {
            "Shopee Pay"
        }
        else -> payChannel
    }
}

fun Int.toAmountDisplay(): String {
    val result = this.toDouble() * 0.01
    return if (result == 0.0) {
        "0.00"
    } else {
        result.toString()
    }
}

fun String.toAmountDisplay(): String {
    val result = this.toDouble() * 0.01
    return if (result == 0.0) {
        "0.00"
    } else {
        result.toString()
    }
}

fun Long.toAmount2DigitDisplay(): String {
    val result = this.toDouble() * 0.01
    return String.format("%.2f", result)
}

fun Int.toAmount2DigitDisplay(): String {
    val result = this.toDouble() * 0.01
    return String.format("%.2f", result)
}

fun String.toAmount2DigitDisplay(): String {
    val result = this.toDouble() * 0.01
    return String.format("%.2f", result)
}