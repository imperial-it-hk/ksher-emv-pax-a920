package com.evp.payment.ksher.parameter

import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil

object CommunicationParam : AbstractParam() {
    private const val PREFIX = "CommunicationParam"

    /**
     * Test data: 6003450000
     */
    val tpdu = SharedPreferencesUtil.getString("$PREFIX.tpdu", "6000000000")

    /**
     * Network International Identifier (NII)
     * Test data: 345
     */
    val nii = SharedPreferencesUtil.getString("$PREFIX.nii", "000")

    /**
     * Socket ip
     */
    val socketIp = SharedPreferencesUtil.getString("$PREFIX.socketIp", "0.0.0.0")

    /**
     * Socket port
     * Test data: 7255
     */
    val socketPort = SharedPreferencesUtil.getString("$PREFIX.socketPort", "1")

    /**
     * Whether to enable backup host
     */
    val backupEnabled = SharedPreferencesUtil.getBoolean("$PREFIX.backupEnabled", false)

    /**
     * Backup socket ip
     */
    val socketIpBackup = SharedPreferencesUtil.getString("$PREFIX.socketIpBackup", "")

    /**
     * Backup socket port
     */
    val socketPortBackup = SharedPreferencesUtil.getString("$PREFIX.socketPortBackup", "")

    /**
     * Whether to enable SSL
     */
    val sslEnabled = SharedPreferencesUtil.getBoolean("$PREFIX.sslEnabled", false)

    /**
     * Connect timeout(s)
     */
    val connectTimeout = SharedPreferencesUtil.getString("$PREFIX.connectTimeout", "20")

    /**
     * Receive timeout(s)
     */
    val receiveTimeout = SharedPreferencesUtil.getString("$PREFIX.receiveTimeout", "40")

    /**
     * Connect retry times
     */
    val reconnectTimes = SharedPreferencesUtil.getString("$PREFIX.reconnectTimes", "3")

    /**
     * Protocol version
     */
    const val protocol = "TLSv1.2"

    /**
     * SSL client cert file
     */
    const val PROD_CERTIFICATE_FILE = ""

    /**
     * SSL private key
     */
    const val PROD_PRIVATE_KEY_FILE = ""

    /**
     * SSL CA file
     */
    const val PROD_CA_FILE = ""
}