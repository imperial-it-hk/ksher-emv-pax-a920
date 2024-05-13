package com.evp.payment.ksher.function.voided

import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.function.voided.view.InputTransactionContact
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.Currency
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.constant.TransStatus
import com.evp.payment.ksher.utils.decrypt
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.ksher.ksher_sdk.Ksher_pay_sdk
import io.reactivex.Completable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import java.lang.NullPointerException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class InputTransactionPresenter @Inject constructor(
    private val view: InputTransactionContact.View,
    private val transactionRepository: TransactionRepository,
    private val configModel: ConfigModel
) : InputTransactionContact.Presenter,
    CoroutineScope {

    private val ksherPay =
        Ksher_pay_sdk(SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt(),
            SystemParam.paymentDomain.get(),
            SystemParam.gateWayDomain.get(),
            SystemParam.publicKey.get(),
            SystemParam.communicationMode.get())
    private lateinit var transData: TransDataModel
    private lateinit var origTransData: TransDataModel
    private var isProcessDone = false

    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    override fun initialTransaction(traceNo: String) {
        launch {
            if (traceNo.isNullOrEmpty()) {
                return@launch
            }
            transactionRepository.getTransactionDetail(traceNo).collect {
                launch(Dispatchers.Main) {
                    val origTransData = it?.getOrNull()
                    if (origTransData == null) {
                        view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionNotFoundLabel?.label))
                        return@launch
                    }
                    if (TransStatus.VOID == origTransData.transType) {
                        view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyVoidLabel?.label))
                        return@launch
                    }

                    if (TransStatus.REFUND == origTransData.transType) {
                        view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyRefundLabel?.label))
                        return@launch
                    }

                    view.showDialogConfirm(origTransData)
                }
            }
        }
    }

    override fun validateOrigTransData(origTransData: TransDataModel) {
        Timber.d(origTransData.toString())
        launch {
            if (origTransData == null) {
                view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionNotFoundLabel?.label))
                return@launch
            }
            if (TransStatus.VOID == origTransData.transType) {
                view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyVoidLabel?.label))
                return@launch
            }

            if (TransStatus.REFUND == origTransData.transType) {
                view.reInputOrFail(StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyRefundLabel?.label))
                return@launch
            }

            // Copy original transaction data
            copyFromOrigTransData(origTransData)

            startVoid()
        }
    }

    private fun startVoid() {
        try {
            launch(Dispatchers.IO) {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance()
                    .primaryContent(StringUtils.getText(configModel.data?.stringFile?.processLabel?.label))

                val response = callVoid(transData)

                val jsonObject = JSONObject(response)
                val dataObject = jsonObject.getJSONObject("data")

                if (!dataObject.has("result")) {
                    throw TransactionException(
                        LocalErrorCode.ERR_RESPONSE,
                        "${
                            jsonObject.getJSONObject("data").getString("err_code")
                        } : ${jsonObject.getJSONObject("data").getString("err_msg")}"
                    )
                }

                when (dataObject.getString("result")) {
                    "SUCCESS" -> {
                        ProgressNotifier.getInstance().show()
                        ProgressNotifier.getInstance()
                            .showApprove(StringUtils.getText(configModel.data?.stringFile?.approvedLabel?.label))
                        delay(2000)
                        saveTransaction()
                    }
                    "FAILURE" -> {

                        val message = if (dataObject.has("err_msg")) {
                            dataObject.getString("err_msg")
                        } else {
                            StringUtils.getText(configModel.data?.stringFile?.declinedLabel?.label)
                        }
                        ProgressNotifier.getInstance().show()
                        ProgressNotifier.getInstance().showDecline(message)
                        delay(2000)
                        ProgressNotifier.getInstance().dismiss()
                        withContext(Dispatchers.Main) {
                            view.onVoidFail(message, transData)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTransaction() {
        launch {
            transactionRepository.addAndUpdate(transData, origTransData).collect {
                // Increase invoice number
                SystemParam.incInvoiceNo()

                ControllerParam.needPrintLastTrans.set(true)
                Completable.fromAction(ProgressNotifier.getInstance()::show)
                    .andThen(TransactionPrinting().printAnyTrans(transData))
                    .doOnComplete {
                        printing()
                    }
                    .doFinally(ProgressNotifier.getInstance()::dismiss)
                    .doOnError { e ->
                        DeviceUtil.beepErr()
                        DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                    }
                    .onErrorComplete()
                    .subscribe()
            }
        }
    }

    private fun copyFromOrigTransData(origTransData: TransDataModel) {
        SystemParam.incTraceNo()
        this.origTransData = origTransData
        transData = TransDataModel.copyFromOrigTransData(origTransData)
        transData.mchRefundNo =
            transData.terminalId + transData.year + transData.date + transData.traceNo

        if ((origTransData.batchNo ?: 0).toInt() == SystemParam.batchNo.get()!!.toInt()) {
            transData.transType = ETransType.VOID.toString()
            this.origTransData.transType = ETransType.VOID.toString()
        } else {
            transData.transType = ETransType.REFUND.toString()
            this.origTransData.transType = ETransType.REFUND.toString()
        }
    }


    private fun printing() {
        launch(Dispatchers.Main) { view.gotoPrinting(transData) }
    }

    private suspend fun callVoid(transData: TransDataModel): String {
        if (SystemParam.appIdOnline.get() == transData.appid) {
            ksherPay.UpdateAppId(SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt())
        } else {
            ksherPay.UpdateAppId(
                SystemParam.appIdOffline.get(),
                SystemParam.tokenOffline.decrypt()
            )
        }

        return try {
            ksherPay.OrderRefund(
                transData.mchRefundNo,
                Currency.THB.name,
                transData.mchOrderNo,
                transData.amount?.toInt(),
                transData.amount?.toInt()
            )
        } catch (e: NullPointerException) {
            e.printStackTrace()
            Timber.e(e)
            ""
        }
    }
}