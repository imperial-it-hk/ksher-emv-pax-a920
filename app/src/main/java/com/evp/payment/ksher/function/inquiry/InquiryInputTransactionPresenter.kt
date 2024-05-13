package com.evp.payment.ksher.function.inquiry

import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.function.inquiry.view.InquiryInputTransactionContact
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.decrypt
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.ksher.ksher_sdk.Ksher_pay_sdk
import io.reactivex.Completable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class InquiryInputTransactionPresenter (
    private val view: InquiryInputTransactionContact.View,
    private val transactionRepository: TransactionRepository,
    private val configModel: ConfigModel
) : InquiryInputTransactionContact.Presenter,
    CoroutineScope {
    private val ksherPay = Ksher_pay_sdk(SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt(),
        SystemParam.paymentDomain.get(),
        SystemParam.gateWayDomain.get(),
        SystemParam.publicKey.get(),
        SystemParam.communicationMode.get())
    private var isProcessDone = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun inquiryAnyTransaction(tracNo: String) {
        ProgressNotifier.getInstance().show()
        ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
        inquiryOnlineProcess(tracNo)
    }

    override fun isProcessTransactionDone(): Boolean = isProcessDone

    private fun inquiryOnlineProcess(tracNo: String) {
        launch(Dispatchers.IO) {
            transactionRepository.getAnyTransaction(tracNo.toLong()).collect {
                val transData = it?.getOrNull()
                if(transData == null){
                    ProgressNotifier.getInstance().dismiss()
                    DeviceUtil.beepErr()
                    CoroutineScope(Dispatchers.Main).launch {
                        view.onTransactionEmpty()
                    }
                } else {
                    if (SystemParam.appIdOnline.get() == it.getOrNull()?.appid) {
                        ksherPay.UpdateAppId(SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt())
                    } else {
                        ksherPay.UpdateAppId(
                            SystemParam.appIdOffline.get(),
                            SystemParam.tokenOffline.decrypt()
                        )
                    }

                    val response = gatewayOrderQuery(it.getOrNull()?.mchOrderNo.orEmpty())
                    val jsonObject = JSONObject(response)
                    val dataObject = jsonObject.getJSONObject("data")
                    val result = dataObject.getString("result")
                    ProgressNotifier.getInstance().dismiss()
                    withContext(Dispatchers.Main) {
                        when (result) {
                            "SUCCESS" -> {
                                ControllerParam.needPrintLastTrans.set(true)
                                Completable.fromAction(ProgressNotifier.getInstance()::show)
                                    .andThen(TransactionPrinting().printAnyTrans(transData))
                                    .doOnComplete {
                                        ControllerParam.needPrintLastTrans.set(false)
                                        view.onTransactionSuccess(transData)
                                        isProcessDone = true
                                    }.doFinally(ProgressNotifier.getInstance()::dismiss)
                                    .doOnError { e ->
                                        isProcessDone = true
                                        DeviceUtil.beepErr()
                                        DialogUtils.showAlertTimeout(
                                            e.message,
                                            DialogUtils.TIMEOUT_FAIL
                                        )
                                    }.onErrorComplete().subscribe()
                            }
                            "FAILURE" -> {
                                view.onTransactionFailure(transData)
                                isProcessDone = true
                            }
                            "NOTSURE" -> {
                                view.showDialogMessage(StringUtils.getText(configModel.data?.stringFile?.pleaseContactAdminLabel?.label))
                            }
                            "PENDING" -> {
                                view.onTransactionTimeout(transData)
                                isProcessDone = true
                            }
                            else -> {
                                view.onTransactionFailure(transData)
                                isProcessDone = true
                            }
                        }
                    }

                    Timber.d("abcdfg $response")
                }
            }
        }
    }

    suspend fun gatewayOrderQuery(mchOrderNo: String): String {
        Timber.d("gatewayOrderQuery transdata : $mchOrderNo")
        return ksherPay.OrderQuery(
            mchOrderNo
        )
    }

}