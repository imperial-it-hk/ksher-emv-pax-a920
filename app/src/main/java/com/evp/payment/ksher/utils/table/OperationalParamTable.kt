package com.evp.payment.ksher.utils.table

import com.evp.payment.ksher.parameter.OperationalParam
import com.evp.payment.ksher.utils.BytesUtil

object OperationalParamTable : BaseCustomTable() {
    const val tableId = "OP"
    fun generate(): String {
        val tableData = StringBuilder()
        // Table name
        tableData.append(BytesUtil.string2HexString(tableId))
        // Version
        tableData.append(version)
        // Whether support Thai
        tableData.append("01")
        // The number of characters per line
        tableData.append("99")
        // The number of lines of prompt text
        // Prompt text lines
        tableData.append("99")
        // Parameter version
        tableData.append(OperationalParam.version.get())
        return addLength(tableData.toString())
    }

    fun parse(data: String) {
        if (data.length < 8) {
            // Parse data error
            return
        }
        // Whether needs to settle before update parameter
        OperationalParam.updateAfterSettle.set("01" == data.substring(6, 8))
        val newVersion = data.substring(0, 6)
        if (OperationalParam.version.get() == newVersion) {
            // Same parameter version, not need to update
            return
        }
        OperationalParam.version.set(newVersion)
        OperationalParam.paramsRawData.set(data.substring(8))
    }
}