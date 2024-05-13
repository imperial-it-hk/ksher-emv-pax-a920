package com.evp.payment.ksher.utils.constant


interface PaymentAction {
    companion object {

        const val KEY_ACTION = "key_action_method"

        const val SALE = "sale_method"

        const val SCAN = "scan_method"

        const val VOID = "void_method"

        const val SETTLEMENT = "settlement_method"

        const val QUERY = "query_method"

        const val PRINT = "print_method"

        const val REPORT = "report_method"

        const val AUDIT_REPORT = "audit_report_method"

        const val GENERATOR_QR = "generator_qr_method"

        const val HISTORY_DETAIL_BY_PAYMENT_TYPE = "history_detail_by_payment_type_method"
    }
}