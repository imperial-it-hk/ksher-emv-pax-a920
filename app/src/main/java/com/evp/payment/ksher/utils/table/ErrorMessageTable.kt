package com.evp.payment.ksher.utils.table

import com.evp.payment.ksher.utils.BytesUtil
import com.google.common.base.Strings

class ErrorMessageTable : BaseCustomTable() {
    var errMsg: String? = null

    companion object {
        const val tableId = "ER"
        fun parse(data: String?): ErrorMessageTable {
            val table = ErrorMessageTable()
            if (!Strings.isNullOrEmpty(data)) {
                table.errMsg = BytesUtil.hexString2String(data, BytesUtil.CHARSET_TIS_620)
            }
            return table
        }
    }
}