 package com.evp.payment.ksher.function.qr

import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.function.qr.view.ScanCodeContact
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.ksher.ksher_sdk.Ksher_pay_sdk
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

class ScanCodePresenter(val view: ScanCodeContact.View):
    CoroutineScope {

//    private val ksherPay = Ksher_pay_sdk(SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt())
    private lateinit var transData: TransDataModel

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO


    private fun checkError(response: String): Single<String> {
        return Single.fromCallable {
            val jsonObject = JSONObject(response)
            if (jsonObject.getJSONObject("data").has("err_code")) {
                /*Completable.error(
                    TransactionException(
                        LocalErrorCode.ERR_RESPONSE,
                        "${jsonObject.getJSONObject("data").has("err_code")} : ${jsonObject.getJSONObject("data").has("err_msg")}"
                    )
                )*/
                throw TransactionException(
                    LocalErrorCode.ERR_RESPONSE,
                    "${
                        jsonObject.getJSONObject("data").getString("err_code")
                    } : ${jsonObject.getJSONObject("data").getString("err_msg")}"
                )
            }
            response
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

}