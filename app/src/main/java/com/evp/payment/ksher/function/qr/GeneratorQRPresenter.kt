package com.evp.payment.ksher.function.qr

import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.function.qr.view.GeneratorQRContact
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.Currency
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
import com.evp.payment.ksher.utils.DateUtils.getFormattedDate
import com.evp.payment.ksher.utils.DateUtils.getFormattedDateTime
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.decrypt
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ksher.ksher_sdk.Ksher_pay_sdk
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

class GeneratorQRPresenter(
    private val view: GeneratorQRContact.View,
    private val transactionRepository: TransactionRepository,
    private val suspendedRepository: SuspendedRepository,
    private val configModel: ConfigModel
) : GeneratorQRContact.Presenter,
    CoroutineScope {
    var appId = SystemParam.appIdOnline.get()
    private val ksherPay = Ksher_pay_sdk(appId, SystemParam.tokenOnline.decrypt(),
        SystemParam.paymentDomain.get(),
        SystemParam.gateWayDomain.get(),
        SystemParam.publicKey.get(),
        SystemParam.communicationMode.get())
    private lateinit var transData: TransDataModel
    private var isProcessDone = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun initialTransaction(payChannel: String, payAmount: String) {
        // increase traceNo
        if (!SystemParam.traceNo.get().equals("000001"))
            SystemParam.incTraceNo()
        val transData = TransDataModel.init()
        if (SystemParam.traceNo.get().equals("000001"))
            SystemParam.incTraceNo()

        transData.paymentChannel = payChannel.toLowerCase()
        transData.transType = ETransType.SALE.toString()
        transData.amount = payAmount.replace(".", "").toLong()

        Completable.fromAction {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.generateQRLabel?.label))
        }
            .andThen(callGenerateQR(transData))
            .doFinally(ProgressNotifier.getInstance()::dismiss)
            .subscribe()
    }

    private fun callGenerateQR(transData: TransDataModel): Completable? {
        return nativePay(transData)
            .flatMapCompletable(this::processResponse)
            .onErrorResumeNext(this::processError)
    }

    private fun processError(e: Throwable): Completable? {
        DeviceUtil.beepErr()
//        if (e is TransactionException) {
        DialogUtils.showAlert(e.message)

//        }
        view.onGenerateQRError(
            e.message ?: StringUtils.getText(configModel.data?.stringFile?.processErrorLabel?.label)
        )
        e.printStackTrace()
        ProgressNotifier.getInstance().dismiss()
        return Completable.complete()
    }

    private fun processResponse(res: String): Completable {
        Timber.d("processResponse : $res")
        val jsonObject = JSONObject(res)
        if (jsonObject.getJSONObject("data").has("code_url") && jsonObject.getJSONObject("data")
                .getString("code_url").isNotBlank()
        ) {
            view.showQr(jsonObject.getJSONObject("data").getString("code_url"))
        } else if (jsonObject.getJSONObject("data").has("imgdat")) {
            view.showQrImage64(jsonObject.getJSONObject("data").getString("imgdat"))
        } else {
            view.onGenerateQRError(StringUtils.getText(configModel.data?.stringFile?.noQRImageFromHostLabel?.label))
            return Completable.complete()
        }

        val qrExpireTime = if (jsonObject.getJSONObject("data").has("tmn_expire_time")) {
            jsonObject.getJSONObject("data").getString("tmn_expire_time")
        } else {
            ""
        }
        CoroutineScope(Dispatchers.IO).launch {
            val suspendedQrDataModel = SuspendedQrDataModel(
                amount = transData.amount,
                traceNo = transData.traceNo,
                batchNo = transData.batchNo,
                mchOrderNo = transData.mchOrderNo,
                paymentChannel = transData.paymentChannel,
                terminalId = transData.terminalId,
                merchantId = transData.merchantId,
                storeId = transData.storeId,
                appid = appId,
                year = transData.year,
                date = transData.date,
                time = transData.time,
                tmLastInitDateTime = transData.tmLastInitDateTime,
                qrCode = jsonObject.getJSONObject("data").getString("imgdat"),
                qrExpireTime = qrExpireTime,
                status = "PENDING"
            )
            suspendedRepository.insertSuspendedQr(suspendedQrDataModel).collect()
        }
        return Completable.complete()
    }

    private fun nativePay(transData: TransDataModel): Single<String> {
        this.transData = transData
        val payChannel = transData.paymentChannel
        if (payChannel == "alipay" || payChannel == "airpay" || payChannel == "linepay") {
            appId = SystemParam.appIdOffline.get()
            ksherPay.UpdateAppId(appId, SystemParam.tokenOffline.decrypt())
        } else if (payChannel == "wechat" || payChannel == "truemoney" || payChannel == "promptpay") {
            appId = SystemParam.appIdOnline.get()
            ksherPay.UpdateAppId(appId, SystemParam.tokenOnline.decrypt())
        } else {
            appId = SystemParam.appIdOnline.get()
            ksherPay.UpdateAppId(appId, SystemParam.tokenOffline.decrypt())
        }

        when {
            "truemoney".equals(payChannel,true) -> {
                return Single.fromCallable {
                    ksherPay.NativePayForTrueMoney(
                        transData.mchOrderNo,
                        Currency.THB.name,
                        transData.paymentChannel,
                        transData.amount,
                        SystemParam.trueMoneyQRTime.get()
                    )
                }.flatMap(this::checkError).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
            }
            "promptpay".equals(payChannel,true) -> {
                return Single.fromCallable {
                    ksherPay.NativePayForPromptPay(
                        transData.mchOrderNo,
                        Currency.THB.name,
                        transData.paymentChannel,
                        transData.amount,
                        SystemParam.prpmptPayQRTime.get()
                    )
                }.flatMap(this::checkError).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
            }
            else -> {
                /* When Initial Sale Transaction */
                /* SAVE Transaction to LocalDB */
                return Single.fromCallable {
                    ksherPay.NativePay(
                        transData.mchOrderNo, Currency.THB.name, transData.paymentChannel, transData.amount
                    )
                }.flatMap(this::checkError).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
            }
        }
    }

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

    override fun inquiryLastTransaction() {
        inquiryOnlineProcess()
    }

    override fun inquiryLastTransactionFinal() {
        inquiryOnlineProcess(true)
    }

    override fun isProcessTransactionDone(): Boolean = isProcessDone

    private fun inquiryOnlineProcess(isFinal: Boolean = false) {
        launch(Dispatchers.IO) {
            val response = gatewayOrderQuery()
            var jsonObject: JSONObject
            try {
                jsonObject = JSONObject(response)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (response.isNotBlank()) {
                        view.onGenerateQRError(response)
                    } else {
                        view.onGenerateQRError(e.message.toString())
                    }
                }
                return@launch
            }
            val dataObject = jsonObject.getJSONObject("data")
            val result = dataObject.getString("result")
            withContext(Dispatchers.Main) {
                if ("SUCCESS".equals(result) && !isProcessDone) {
                    isProcessDone = true
                    view.stopCountdown()
                    view.stopAutoInquiry()
                    setAmount(dataObject)
                    setTimeToTransData(dataObject)
                    setRefNoToTransData(dataObject)
                    setTransactionId(dataObject)
                    setInvoiceNo(dataObject)
                    setAppId(dataObject)
                    transactionRepository.insertTransaction(transData).collect {
                        // REMOVE ROW FROM SUSPENDED QR WHEN TRANSACTION SUCCESSFULLY.
                        suspendedRepository.updateSuspendedQrWithTraceNo(
                            traceNo = transData.traceNo.toString(),
                            "SUCCESS"
                        ).collect()

                        SystemParam.incInvoiceNo()
                        ControllerParam.needPrintLastTrans.set(true)
                        Completable.fromAction(ProgressNotifier.getInstance()::show)
                            .andThen(TransactionPrinting().printAnyTrans(transData))
                            .doOnComplete {
                                ControllerParam.needPrintLastTrans.set(false)
                                view.onTransactionSuccess(transData)
                            }
                            .doFinally(ProgressNotifier.getInstance()::dismiss)
                            .doOnError { e ->
                                Timber.e(e)
                                e.printStackTrace()
                                DeviceUtil.beepErr()
                                DialogUtils.showAlertTimeout(
                                    e.message,
                                    DialogUtils.TIMEOUT_FAIL
                                )
                            }
                            .subscribe()
                    }
                } else if ("FAILURE".equals(result) && !isProcessDone) {
                    view.onTransactionFailure(transData)
                    isProcessDone = true
                } else if ("NOTSURE".equals(result) && isFinal && !isProcessDone) {
                    view.showDialogMessage(StringUtils.getText(configModel.data?.stringFile?.pleaseContactAdminLabel?.label))
                    isProcessDone = true
                } else if ("PENDING".equals(result) && isFinal && !isProcessDone) {
                    view.onTransactionTimeout(transData)
                    isProcessDone = true
                }
            }
            Timber.d("abcdfg $response")
        }
    }

    private fun setAmount(dataObject: JSONObject) {
        if (dataObject.has("total_fee")) {
            transData.amount = dataObject.getLong("total_fee")
        }
        if (dataObject.has("cash_fee")) {
            transData.amountConvert = dataObject.getLong("cash_fee")
        }
        if (dataObject.has("cash_fee_type")) {
            transData.currencyConvert = dataObject.getString("cash_fee_type")
        }
        if (dataObject.has("rate")) {
            transData.exchangeRate = dataObject.getString("rate")
        }
    }

    private fun setInvoiceNo(dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val invoiceNo =
            transData.terminalId + getFormattedDate(
                timeEnd,
                "yyyy-MM-dd HH:mm:ss",
                "YYMMddHHmmss"
            )
        transData.invoiceNo = invoiceNo
    }

    private fun setTransactionId(dataObject: JSONObject) {
        val transId = if (dataObject.has("channel_order_no")) {
            dataObject.getString("channel_order_no")
        } else {
            transData.mchOrderNo
        }

        transData.transactionId = transId
    }

    private fun setRefNoToTransData(dataObject: JSONObject) {
        val refNo = dataObject.getString("ksher_order_no")
        transData.referNo = refNo
    }

    private fun setAppId(dataObject: JSONObject) {
        val appId = dataObject.getString("appid")
        transData.appid = appId
    }


    private fun setTimeToTransData(dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val date = getFormattedDateTime(timeEnd, "yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        calendar.time = date
        transData.date = calendar.getAbbreviatedFromDateTime("MMdd")
        transData.year = calendar.getAbbreviatedFromDateTime("YYYY")
        transData.time = calendar.getAbbreviatedFromDateTime("HHmmss")
    }

    suspend fun gatewayOrderQuery(): String {
        try {
            Timber.d("gatewayOrderQuery transdata : ${transData.mchOrderNo}")
            return ksherPay.OrderQuery(
                transData.mchOrderNo
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return e.message.toString()
        }
    }

}