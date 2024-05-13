package com.evp.payment.ksher.utils.table

import com.evp.payment.ksher.utils.BytesUtil

object BuyerCodeTable : BaseCustomTable() {
    const val tableId = "BC"
    fun generate(uuid: String?, rvCode: String?, buyerCode: String?): String {
        val tableData = StringBuilder()
        // Table name
        tableData.append(BytesUtil.string2HexString(tableId))
        // Version
        tableData.append(version)
        // UUID
        tableData.append(uuid)
        // RV Code
        tableData.append(rvCode)
        // Buyer Code
        tableData.append(BytesUtil.string2HexString(buyerCode))
        return addLength(tableData.toString())
    }
}