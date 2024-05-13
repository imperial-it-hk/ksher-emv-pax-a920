package com.evp.payment.ksher.utils

import android.os.Build
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

object Secret {
    fun secret() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048)
            val kp: KeyPair = kpg.generateKeyPair()
            var privateKey: String =
                Base64.getMimeEncoder().encodeToString(kp.getPrivate().getEncoded())
            var publicKey: String = Base64.getMimeEncoder().encodeToString(
                kp.public.encoded
            )

            privateKey = privateKey.replace("\n", "").replace("\r", "")
            publicKey = publicKey.replace("\n", "").replace("\r", "")
            println("-------------------- PRIVATE KEY --------------------")
            println(privateKey)
            println("--------------------  --------------------")
            println("-------------------- PUBLIC KEY --------------------")
            println(publicKey)
            println("--------------------  --------------------")
        }
    }
}