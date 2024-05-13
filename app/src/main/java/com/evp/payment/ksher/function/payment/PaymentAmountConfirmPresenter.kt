package com.evp.payment.ksher.function.payment

import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.function.payment.view.PaymentAmountConfirmContact
import com.evp.payment.ksher.parameter.Currency
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
import com.evp.payment.ksher.utils.DateUtils.getFormattedDate
import com.evp.payment.ksher.utils.DateUtils.getFormattedDateTime
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.evp.eos.utils.LogUtil
import com.evp.payment.ksher.utils.decrypt
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
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PaymentAmountConfirmPresenter @Inject constructor(
    private val view: PaymentAmountConfirmContact.View,
    private val transactionRepository: TransactionRepository,
    private val suspendedRepository: SuspendedRepository,
    private val configModel: ConfigModel
) : PaymentAmountConfirmContact.Presenter,
    CoroutineScope {

    private var appId = SystemParam.appIdOffline.get()
    private var ksherPay = Ksher_pay_sdk(appId, SystemParam.tokenOffline.decrypt(),
        SystemParam.paymentDomain.get(),
        SystemParam.gateWayDomain.get(),
        SystemParam.publicKey.get(),
        SystemParam.communicationMode.get())
    private lateinit var transData: TransDataModel
    private var isProcessDone = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun initialTransaction(payChannel: String, payAmount: String, authCode: String) {

        appId = SystemParam.appIdOffline.get()
        ksherPay.UpdateAppId(appId, SystemParam.tokenOffline.decrypt())

        view.displayAmount()

        // increase traceNo
        if (!SystemParam.traceNo.get().equals("000001"))
            SystemParam.incTraceNo()
        val transData = TransDataModel.init()
        if (SystemParam.traceNo.get().equals("000001"))
            SystemParam.incTraceNo()

        transData.paymentChannel = payChannel.lowercase(Locale.getDefault())
        transData.transType = ETransType.SALE.toString()
        transData.amount = payAmount.replace(".", "").toLong()
        transData.authCode = authCode

//        LogUtil.d("QUICKPAY", (0..100000).random().toString() + " " +  payChannel.lowercase(Locale.getDefault())+ " " + ETransType.SALE.toString()+ " " + payAmount.replace(".", "").toLong()+ " " +authCode)
        Completable.fromAction {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.processLabel?.label))
        }
            .andThen(callQuickPay(transData))
            .doFinally(ProgressNotifier.getInstance()::dismiss)
            .subscribe()
    }

    private fun quickPay(transData: TransDataModel): Single<String> {
        this.transData = transData

        /* When Initial Sale Transaction */
        /* SAVE Transaction to LocalDB */

        return Single.fromCallable {
            ksherPay.QuickPay(
                transData.mchOrderNo,
                Currency.THB.name,
                transData.authCode,
                transData.paymentChannel,
                "",
                transData.amount?.toInt()
            )
        }.flatMap(this::checkError)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io());
    }

    private fun checkError(response: String): Single<String> {
        return Single.fromCallable {
            val jsonObject = JSONObject(response)
            LogUtil.d("QUICKPAY", jsonObject.toString())
            if (jsonObject.getJSONObject("data").has("err_code")) {
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

    private fun callQuickPay(transData: TransDataModel): Completable? {
        return quickPay(transData)
            .flatMapCompletable(this::processResponse)
            .onErrorResumeNext(this::processError)
    }

    private fun processError(e: Throwable): Completable? {
        DeviceUtil.beepErr()
        DialogUtils.showAlert(e.message)
        view.onQuickPayError(e.message ?: "processError", transData)
        e.printStackTrace()
        ProgressNotifier.getInstance().dismiss()
        return Completable.complete()
    }

    private fun processResponse(res: String): Completable {
        val jsonObject = JSONObject(res)
        if (jsonObject.getJSONObject("data").getString("result").equals("SUCCESS", true)) {
            LogUtil.d("QUICKPAY", jsonObject.toString())
            inquiryOnlineProcess()
        }
        return Completable.complete()
    }

    private fun inquiryOnlineProcess() {
        launch(Dispatchers.IO) {
            val response = gatewayOrderQuery()
            val jsonObject = JSONObject(response)
            val dataObject = jsonObject.getJSONObject("data")
            val result = dataObject.getString("result")
            withContext(Dispatchers.Main) {
                when (result) {
                    "SUCCESS" -> {
                        // UPDATE STATUS SUSPENDED QR
                        suspendedRepository.updateSuspendedQrWithTraceNo(
                            traceNo = transData.traceNo.toString(),
                            "SUCCESS"
                        ).collect()
                        setAmount(dataObject)
                        setTimeToTransData(dataObject)
                        setRefNoToTransData(dataObject)
                        setTransactionId(dataObject)
                        setInvoiceNo(dataObject)
                        setAppId(dataObject)
                        view.onTransactionSuccess(transData)
                        isProcessDone = true
                    }
                    "FAILURE" -> {
                        view.onTransactionFailure(transData)
                        isProcessDone = true
                    }
                    "NOTSURE" -> {
                        view.showDialogMessage(transData, StringUtils.getText(configModel.data?.stringFile?.pleaseContactAdminLabel?.label))
                        isProcessDone = true
                    }
                    "PENDING" -> {
                        view.onTransactionTimeout(transData)
                        isProcessDone = true
                    }
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
            transData.terminalId + getFormattedDate(timeEnd, "yyyy-MM-dd HH:mm:ss", "YYMMddHHmmss")
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

    private fun setAppId(dataObject: JSONObject) {
        val appId = dataObject.getString("appid")
        transData.appid = appId
    }

    private fun setRefNoToTransData(dataObject: JSONObject) {
        val refNo = dataObject.getString("ksher_order_no")
        transData.referNo = refNo
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
                qrCode = "",
                qrExpireTime = "",
                status = "PENDING"
            )

            suspendedRepository.insertSuspendedQr(suspendedQrDataModel).collect()
        }
        Timber.d("gatewayOrderQuery transdata : ${transData.mchOrderNo}")
        return ksherPay.OrderQuery(
            transData.mchOrderNo
        )
    }

}