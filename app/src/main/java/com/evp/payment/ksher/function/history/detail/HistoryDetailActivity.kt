package com.evp.payment.ksher.function.history.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.InputPasswordDialog
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.Currency
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.*
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.constant.TransStatus
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.ksher.ksher_sdk.Ksher_pay_sdk
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_history_detail.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HistoryDetailActivity : BaseTimeoutActivity() {
    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    private val ksherPay : Ksher_pay_sdk by lazy {
        Ksher_pay_sdk(
            SystemParam.appIdOnline.get(),
            SystemParam.tokenOnline.decrypt(),
            SystemParam.paymentDomain.get(),
            SystemParam.gateWayDomain.get(),
            SystemParam.publicKey.get(),
            SystemParam.communicationMode.get()
        )
    }
    lateinit var displayTransData: TransDataModel
    private lateinit var origTransData: TransDataModel
    lateinit var transData: TransDataModel

    private val mchOrderNo by extraNotNull<String>("mchOrderNo")

    private val isInvoke by extra("is_invoke", false)

    var historyDataList: ArrayList<HistoryData> = arrayListOf()

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_history_detail

    override fun initViews() {
        layoutSubHeader.visible()
        tvSubHeaderTitle.visible()
        tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.transactionHistoryLabel?.label)

        btnPrint.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)
        btnVoid.text = StringUtils.getText(configModel.data?.stringFile?.voidAndRefundLabel?.label)

        setThemePrimaryColor(btnPrint)
        layout_back.setOnClickListener { onBackPressed() }

        try {
            CoroutineScope(Dispatchers.IO).launch {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance()
                    .primaryContent(StringUtils.getText(configModel.data?.stringFile?.checkTransactionLabel?.label))
                transactionRepository.getTransactionDetailLatest(mchOrderNo)
                    .collect { transData ->
                        ProgressNotifier.getInstance().dismiss()
                        Timber.d("transData $transData")
                        val trans = transData?.getOrNull()
                        trans?.let {
                            this@HistoryDetailActivity.displayTransData = trans
                            CoroutineScope(Dispatchers.Main).launch {
                                display(trans)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btnPrint.setOnClickListener {
            ProgressNotifier.getInstance().dismiss()
            ControllerParam.needPrintLastTrans.set(true)
            Completable.fromAction(ProgressNotifier.getInstance()::show)
                .andThen(TransactionPrinting(true).printAnyTrans(displayTransData, 0))
                .doOnComplete {
                    DeviceUtil.beepOk()
                    ControllerParam.needPrintLastTrans.set(false)
                    val bundle = Bundle()
                    bundle.putParcelable("transData", displayTransData)
                    bundle.putBoolean("re_print", true)
                    startActivity(PrintActivity::class.java, bundle)
                    finish()
                }
                .doFinally(ProgressNotifier.getInstance()::dismiss)
                .onErrorComplete()
                .subscribe()
        }

        btnVoid.setOnClickListener {
            val passwordDialog =
                InputPasswordDialog(
                    this,
                    InputPasswordDialog.Builder(this, PasswordType.VOID, object :
                        InputPasswordDialog.OnPasswordActionListener {
                        override fun onSuccess(dialogFragment: InputPasswordDialog) {
                            dialogFragment.dismiss()
                            initialTransaction(displayTransData.traceNo.toString())
                        }

                        override fun onFail(dialogFragment: InputPasswordDialog) {
                            dialogFragment.dismiss()
                        }

                        private fun startVoid() {

                        }


                    })
                )
            passwordDialog.show()
        }

    }

    private fun showDialogResumeNext(it: Throwable): Completable {
        return Single.fromCallable { showRxDialog(it.message ?: "", {}, {}) }
            .observeOn(AndroidSchedulers.mainThread())
            .ignoreElement();
    }

    private fun getMessageTitle(): String = "ABC"

    private fun display(trans: TransDataModel) {
        if (trans.transType == ETransType.VOID.toString() || trans.transType == ETransType.REFUND.toString()) {
            layoutVoidOrRefund.visible()
            btnVoid.gone()
            if (trans.transType == ETransType.VOID.toString()) {
                tvVoidDateTimeLabel.text = "VOID DateTime"
                tvVoidAmountLabel.text = "VOID AMOUNT"
            } else {
                tvVoidDateTimeLabel.text = "REFUND DateTime"
                tvVoidAmountLabel.text = "REFUND AMOUNT"
            }
            tvVoidDateTime.text = DateUtils.getFormattedDate(
                "${trans.year + trans.date} ${trans.time}",
                "yyyyMMdd HHmmss",
                "MMM dd, yy HH:mm:ss"
            )
            tvVoidAmount.text = "THB  -${trans.amount?.toAmount2DigitDisplay()}"

            tvTranType.text = trans.origTransType
            tvTraceNo.text = String.format("%06d", trans.origTraceNo!!)
            tvBatchNo.text = String.format("%06d", trans.origBatchNo!!)
            tvTranDateTime.text = DateUtils.getFormattedDate(
                "${trans.origYear + trans.origDate} ${trans.origTime}",
                "yyyyMMdd HHmmss",
                "MMM dd, yy HH:mm:ss"
            )

        } else {
            layoutVoidOrRefund.gone()

            tvTranType.text = trans.transType
            tvTraceNo.text = String.format("%06d", trans.traceNo!!)
            tvBatchNo.text = String.format("%06d", trans.batchNo!!)
            tvTranDateTime.text = DateUtils.getFormattedDate(
                "${trans.year + trans.date} ${trans.time}",
                "yyyyMMdd HHmmss",
                "MMM dd, yy HH:mm:ss"
            )
        }
        setupSubHeaderLogo(ivProviderImage, trans.paymentChannel!!)
        tvProviderName.text = trans.paymentChannel?.toPaymentChannelDisplay()
        tvAmount.text = "THB  ${trans.amount?.toAmount2DigitDisplay()}"
        tvTransactionId.text = trans.transactionId
        tvInvoiceNo.text = trans.invoiceNo.toString()
    }

    protected fun startActivity(cls: Class<*>?, bundle: Bundle) {
        val intent = Intent(this, cls).apply {
            flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
            putExtras(bundle)
        }
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
//        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun initialTransaction(traceNo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            transactionRepository.getTransactionDetail(
                ETransType.SALE.toString(), traceNo
            ).collect {
                validateOrigTransData(it?.getOrNull())
            }
        }
    }

    private fun validateOrigTransData(origTransData: TransDataModel?) {
        Timber.d(origTransData.toString())

        if (origTransData == null) {
            showDialog(
                msg = StringUtils.getText(configModel.data?.stringFile?.transactionNotFoundLabel?.label),
                actionCancel = {},
                actionConfirm = {})
            return
        }
        if (TransStatus.VOID.equals(origTransData.transType, true)) {
            showDialog(
                msg = StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyVoidLabel?.label),
                actionCancel = {},
                actionConfirm = {})
            return
        }

        if (TransStatus.REFUND.equals(origTransData.transType, true)) {
            showDialog(
                msg = StringUtils.getText(configModel.data?.stringFile?.transactionAlreadyRefundLabel?.label),
                actionCancel = {},
                actionConfirm = {})
            return
        }

        // Copy original transaction data
        copyFromOrigTransData(origTransData)

        startVoid()
    }

    private fun startVoid() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance().primaryContent("Processing")

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

                val result = dataObject.getString("result")
                when (result) {
                    "SUCCESS" -> {
                        ProgressNotifier.getInstance().show()
                        ProgressNotifier.getInstance().showApprove("Approved")
                        delay(2000)
                        saveTransaction()
                    }
                    "FAILURE" -> {
                        val message = if (dataObject.has("err_msg")) {
                            dataObject.getString("err_msg")
                        } else {
                            "Declined"
                        }
                        ProgressNotifier.getInstance().show()
                        ProgressNotifier.getInstance().showDecline(message)
                        delay(2000)
                        ProgressNotifier.getInstance().dismiss()
                        withContext(Dispatchers.Main) {
                            showDialog(msg = message, actionConfirm = {}, actionCancel = {})
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTransaction() {
        CoroutineScope(Dispatchers.IO).launch {
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
        ControllerParam.needPrintLastTrans.set(false)
        val bundle = Bundle()
        bundle.putParcelable("transData", transData)
        startActivity(PrintActivity::class.java, bundle)
        finish()
    }

    private suspend fun callVoid(transData: TransDataModel): String {
        if (SystemParam.appIdOnline.get() == transData.appid) {
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

    companion object {
        @JvmStatic
        fun start(context: Activity?, mchOrderNo: String?) {
            val intent =  Intent(context, HistoryDetailActivity::class.java)
            intent.putExtra("mchOrderNo", mchOrderNo)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, mchOrderNo: String?) {
            val intent =  Intent(context, HistoryDetailActivity::class.java)
            intent.putExtra("mchOrderNo", mchOrderNo)
            intent.putExtra("is_invoke", true)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }
}
