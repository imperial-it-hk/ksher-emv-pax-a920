package com.evp.payment.ksher.function.main

import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.function.main.view.PaymentActionTypeContact
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
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
import java.util.*
import kotlin.coroutines.CoroutineContext

class PaymentActionTypePresenter(
    private val view: PaymentActionTypeContact.View,
    private val transactionRepository: TransactionRepository,
    private val suspendedRepository: SuspendedRepository,
    private val configModel: ConfigModel
) : PaymentActionTypeContact.Presenter,
    CoroutineScope {
    private val ksherPay = Ksher_pay_sdk(SystemParam.appIdOnline.get(), SystemParam.tokenOffline.decrypt(),
        SystemParam.paymentDomain.get(),
        SystemParam.gateWayDomain.get(),
        SystemParam.publicKey.get(),
        SystemParam.communicationMode.get())
    private var isProcessDone = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO


    override fun inquiryLastTransaction() {
        ProgressNotifier.getInstance().show()
        ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
        inquiryOnlineProcess()
    }

    override fun inquiryTransaction(traceNo: String) {
        ProgressNotifier.getInstance().show()
        ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
        inquiryOnlineProcess(traceNo)
    }

    override fun isProcessTransactionDone(): Boolean = isProcessDone

    override fun printLastTransaction() {
        launch(Dispatchers.IO) {
            transactionRepository.getTransactionLatest().collect {
                val transData = it?.getOrNull()
                if (transData == null) {
                    ProgressNotifier.getInstance().dismiss()
                    DeviceUtil.beepErr()
                    CoroutineScope(Dispatchers.Main).launch {
                        view.onInquiryTransactionEmpty()
                    }
                } else {
                    view.gotoPrintLastTran(transData, true, StringUtils.getText(configModel.data?.stringFile?.printLabel?.label),
                        StringUtils.getText(configModel.data?.stringFile?.lastTransactionLabel?.label))
//                    ProgressNotifier.getInstance().dismiss()
//                    Completable.fromAction(ProgressNotifier.getInstance()::show)
//                        .andThen(TransactionPrinting(true).printAnyTrans(transData, 0)).doOnComplete {
//                            view.gotoPrintLastTran(transData, true, "Print", "Last Transaction")
//                            isProcessDone = true
//                        }.doFinally(ProgressNotifier.getInstance()::dismiss).doOnError { e ->
//                            isProcessDone = true
//                            DeviceUtil.beepErr()
//                            CoroutineScope(Dispatchers.Main).launch {
//                                view.onInquiryTransactionEmpty()
//                            }
//                        }.onErrorComplete().subscribe()
                }
            }
        }
    }

    private fun inquiryOnlineProcess(traceNo: String? = null) {
        launch(Dispatchers.IO) {
            try {
                if (traceNo.isNullOrEmpty()) {
                    suspendedRepository.querySuspendedQRLatest().collect {
                        val suspendedQrData = it?.getOrNull()
                        inquiryOnlineProcess(suspendedQrData)
                    }
                } else {
                    suspendedRepository.querySuspendedQR(traceNo).collect {
                        val suspendedQrData = it?.getOrNull()
                        inquiryOnlineProcess(suspendedQrData)
                    }
                }
            }catch (e :Exception){

            }
        }
    }

    suspend fun inquiryOnlineProcess(suspendedQrData: SuspendedQrDataModel?) {
        coroutineScope {
            if (suspendedQrData == null) {
                ProgressNotifier.getInstance().dismiss()
                DeviceUtil.beepErr()
                CoroutineScope(Dispatchers.Main).launch {
                    view.onInquiryTransactionEmpty()
                }
            } else {
                transactionRepository.getTransactionDetailLatest(suspendedQrData.mchOrderNo!!)
                    .collect {
                        val transData = it?.getOrNull()
                        if (transData != null) {
                            ProgressNotifier.getInstance().dismiss()
                            isProcessDone = true
                            view.gotoPrintLastTran(transData, true, StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label),
                                StringUtils.getText(configModel.data?.stringFile?.lastTransactionLabel?.label))

                            ControllerParam.needPrintLastTrans.set(true)
//                                Completable.fromAction(ProgressNotifier.getInstance()::show)
//                                    .andThen(TransactionPrinting().rePrintAnyTrans(transData, 0))
//                                    .doOnComplete {
//                                        ControllerParam.needPrintLastTrans.set(false)
//                                        view.onInquiryTransactionSuccess(transData, true)
//                                        isProcessDone = true
//                                    }.doFinally(ProgressNotifier.getInstance()::dismiss)
//                                    .doOnError { e ->
//                                        isProcessDone = true
//                                        DeviceUtil.beepErr()
//                                        DialogUtils.showAlertTimeout(
//                                            e.message,
//                                            DialogUtils.TIMEOUT_FAIL
//                                        )
//                                    }.onErrorComplete().subscribe()
                        } else {
                            if (SystemParam.appIdOnline.get() == suspendedQrData.appid) {
                                ksherPay.UpdateAppId(
                                    SystemParam.appIdOnline.get(),
                                    SystemParam.tokenOnline.decrypt()
                                )
                            } else {
                                ksherPay.UpdateAppId(
                                    SystemParam.appIdOffline.get(),
                                    SystemParam.tokenOffline.decrypt()
                                )
                            }
                            val response =
                                gatewayOrderQuery(suspendedQrData.mchOrderNo.orEmpty())
                            val jsonObject = JSONObject(response)
                            val dataObject = jsonObject.getJSONObject("data")
                            val result = dataObject.getString("result")
                            Timber.d("response $response")
                            launch(Dispatchers.Main) {
                                when (result) {
                                    "SUCCESS" -> {
                                        suspendedRepository.updateSuspendedQrWithTraceNo(
                                            suspendedQrData.traceNo.toString(),
                                            "SUCCESS"
                                        )
                                        val transData =
                                            TransDataModel.initFromSuspendedQr(suspendedQrData)
                                        setAmount(transData, dataObject)
                                        setTimeToTransData(transData, dataObject)
                                        setRefNoToTransData(transData, dataObject)
                                        setTransactionId(transData, dataObject)
                                        setInvoiceNo(transData, dataObject)
                                        setAppId(transData, dataObject)

                                        transactionRepository.insertTransaction(transData)
                                            .collect {
                                                // REMOVE ROW FROM SUSPENDED QR WHEN TRANSACTION SUCCESSFULLY.
                                                suspendedRepository.updateSuspendedQrWithTraceNo(
                                                    traceNo = transData.traceNo.toString(),
                                                    "SUCCESS"
                                                )
                                                    .collect()
                                                SystemParam.incInvoiceNo()

                                                ControllerParam.needPrintLastTrans.set(true)
                                                Completable.fromAction(ProgressNotifier.getInstance()::show)
                                                    .andThen(
                                                        TransactionPrinting().printAnyTrans(
                                                            transData
                                                        )
                                                    )
                                                    .doOnComplete {
                                                        ControllerParam.needPrintLastTrans.set(
                                                            false
                                                        )
                                                        view.onInquiryTransactionSuccess(
                                                            transData,
                                                            false
                                                        )

                                                        isProcessDone = true
                                                    }
                                                    .doFinally(ProgressNotifier.getInstance()::dismiss)
                                                    .doOnError { e ->
                                                        ProgressNotifier.getInstance()::dismiss
                                                        isProcessDone = true
                                                        DeviceUtil.beepErr()
                                                        DialogUtils.showAlertTimeout(
                                                            e.message,
                                                            DialogUtils.TIMEOUT_FAIL
                                                        )
                                                    }.onErrorComplete().subscribe()
                                            }
                                    }
                                    "FAILURE" -> {
                                        view.onInquiryTransactionFailure(
                                            dataObject.getString(
                                                "err_msg"
                                            )
                                        )

                                        isProcessDone = true
                                        ProgressNotifier.getInstance().dismiss()

                                    }
                                    "NOTSURE" -> {
                                        view.onInquiryTransactionFailure(
                                            "Please contact administrator"
                                        )

                                        isProcessDone = true
                                        ProgressNotifier.getInstance().dismiss()
                                    }
                                    "PENDING" -> {
                                        view.onInquiryTransactionFailure(
                                            dataObject.getString(
                                                "err_msg"
                                            )
                                        )

                                        isProcessDone = true
                                        ProgressNotifier.getInstance().dismiss()
                                    }
                                    else -> {
                                        view.onInquiryTransactionFailure(
                                            "Unknown"
                                        )
                                        isProcessDone = true
                                        ProgressNotifier.getInstance().dismiss()
                                    }
                                }
                            }

                            Timber.d("abcdfg $response")
                        }
                    }
            }
        }
    }

    private fun setAmount(transData: TransDataModel, dataObject: JSONObject) {
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

    private fun setInvoiceNo(transData: TransDataModel, dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val invoiceNo =
            transData.terminalId + DateUtils.getFormattedDate(
                timeEnd,
                "yyyy-MM-dd HH:mm:ss",
                "YYMMddHHmmss"
            )
        transData.invoiceNo = invoiceNo
    }

    private fun setTransactionId(transData: TransDataModel, dataObject: JSONObject) {
        val transId = if (dataObject.has("channel_order_no")) {
            dataObject.getString("channel_order_no")
        } else {
            transData.mchOrderNo
        }

        transData.transactionId = transId
    }

    private fun setRefNoToTransData(transData: TransDataModel, dataObject: JSONObject) {
        val refNo = dataObject.getString("ksher_order_no")
        transData.referNo = refNo
    }

    private fun setAppId(transData: TransDataModel, dataObject: JSONObject) {
        val appId = dataObject.getString("appid")
        transData.appid = appId
    }


    private fun setTimeToTransData(transData: TransDataModel, dataObject: JSONObject) {
        val timeEnd = dataObject.getString("time_end")
        val date = DateUtils.getFormattedDateTime(timeEnd, "yyyy-MM-dd HH:mm:ss")
        val calendar = Calendar.getInstance()
        calendar.time = date
        transData.date = calendar.getAbbreviatedFromDateTime("MMdd")
        transData.year = calendar.getAbbreviatedFromDateTime("YYYY")
        transData.time = calendar.getAbbreviatedFromDateTime("HHmmss")
    }

    suspend fun gatewayOrderQuery(mchOrderNo: String): String {
        Timber.d("gatewayOrderQuery transdata : $mchOrderNo")
        return ksherPay.OrderQuery(
            mchOrderNo
        )
    }

}