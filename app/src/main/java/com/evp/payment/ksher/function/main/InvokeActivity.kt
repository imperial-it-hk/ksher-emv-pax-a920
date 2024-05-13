package com.evp.payment.ksher.function.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.evp.payment.ksher.function.payment.action.InputAmountActivity
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.pax.unifiedsdk.message.MessageUtils
import com.pax.unifiedsdk.message.SaleMsg
import com.pax.unifiedsdk.message.ScanQRCodeMsg
import timber.log.Timber


class InvokeActivity : AppCompatActivity() {

    val REQUEST_SALE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request = MessageUtils.generateRequest(intent)
        val commandType = MessageUtils.getType(request)
        Log.d(InvokeActivity::class.java.simpleName,"InvokeActivity data" + commandType)
        when(commandType) {
            15 -> {
                // 1 = Scan
                startScan(request as ScanQRCodeMsg.Request)
                finish()
            }
            2 -> {
                // 2 = Sale
                startSale(request as SaleMsg.Request)
                finish()
            }
        }
    }

    private fun startScan(request : ScanQRCodeMsg.Request) {
        val intent = Intent(this, InputAmountActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SCAN)
        val amount = request.amount
        startActivity(intent)
    }

    private fun startSale(request : SaleMsg.Request) {
        val intent = Intent(this, InputAmountActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SALE)
        val amount = request.amount
        val tipAMount = request.tipAmount
        startActivity(intent)
    }
}