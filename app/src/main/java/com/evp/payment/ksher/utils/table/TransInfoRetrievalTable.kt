package com.evp.payment.ksher.utils.table

import com.evp.payment.ksher.utils.BytesUtil

object TransInfoRetrievalTable : BaseCustomTable() {
    const val tableId = "TI"
    fun generate(retrievalKey: String?): String {
        val tableData = StringBuilder()
        // Table name
        tableData.append(BytesUtil.string2HexString(tableId))
        // Version
        tableData.append(version)
        // Transaction Retrieval Key
        tableData.append(retrievalKey)
        return addLength(tableData.toString())
    }
}