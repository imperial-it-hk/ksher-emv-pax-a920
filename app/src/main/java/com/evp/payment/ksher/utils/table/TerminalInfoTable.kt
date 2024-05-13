package com.evp.payment.ksher.utils.table

import android.os.Build
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.utils.BytesUtil
import com.google.common.base.Strings

object TerminalInfoTable : BaseCustomTable() {
    const val tableId = "TM"


    private fun appendStoreId(tableData: StringBuilder, storeId: String) {
        if (Strings.isNullOrEmpty(storeId)) {
            // Store ID length
            tableData.append("00")
        } else {
            // Store ID length
            tableData.append(Strings.padStart(storeId.length.toString(), 2, '0'))
            // Store ID
            tableData.append(BytesUtil.string2HexString(storeId))
        }
    }
}