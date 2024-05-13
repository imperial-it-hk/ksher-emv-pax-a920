package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

/**
 * Transaction process control parameters
 */
object ControllerParam {
    private const val PREFIX = "ControllerParam"

    /**
     * Whether settlement is required
     */
    val needSettlement = SharedPreferencesUtil.getBoolean("$PREFIX.needSettlement", false)

    /**
     * Whether the transaction records need to be cleared
     */
    val needClearTrans = SharedPreferencesUtil.getBoolean("$PREFIX.needClearTrans", false)

    /**
     * Whether to clear the reversal records
     */
    val needClearReverse = SharedPreferencesUtil.getBoolean("$PREFIX.needClearReverse", false)

    /**
     * Whether to use backup host as current communication address
     */
    val useBackupIp = SharedPreferencesUtil.getBoolean("$PREFIX.useBackupIp", false)

    /**
     * Whether needs to reprint latest transaction
     */
    val needPrintLastTrans = SharedPreferencesUtil.getBoolean("$PREFIX.needPrintLastTrans", false)

    /**
     * Whether needs to print latest settlement
     */
    val needPrintLastSettle =
        SharedPreferencesUtil.getBoolean("$PREFIX.needPrintLastSettle", false)

    /**
     * Latest settlement information
     */
//    val lastSettleInformation = SharedPreferencesUtil.getObject<SettleInformation>(
//        "$PREFIX.lastSettleInformation",
//        SettleInformation::class.java
//    )
}