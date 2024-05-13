package com.evp.payment.ksher.function.settlement

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.invoke.InvokeResponseDescriptionData
import com.evp.payment.ksher.invoke.InvokeResponseHeaderData
import com.evp.payment.ksher.invoke.InvokeResponseModel
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DateUtils.getAbbreviatedFromDateTime
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.InvokeConstant
import com.evp.payment.ksher.utils.constant.KsherConstant
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.decrypt
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import com.ksher.ksher_sdk.Ksher_pay_sdk
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_settlement.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvPaymentChannel
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvRefundCount
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvRefundTotalAmount
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvSaleCount
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvSaleTotalAmount
import kotlinx.android.synthetic.main.settlement_grand_total_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SettlementActivity : BaseTimeoutActivity() {

    private val ksherPay: Ksher_pay_sdk by lazy {
        Ksher_pay_sdk(
            SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt(),
            SystemParam.paymentDomain.get(),
            SystemParam.gateWayDomain.get(),
            SystemParam.publicKey.get(),
            SystemParam.communicationMode.get()
        )
    }

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository
    var paymentChannelList =
        arrayListOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")
    var settlementModel = SettlementModel()
    var grandTotalAmount = SettlementItemModel()

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_settlement

    override fun initViews() {
        if (configModel?.data?.menusSale?.payment?.isNotEmpty() == true) {
            paymentChannelList = arrayListOf()
            configModel?.data?.menusSale?.payment?.forEach {
                it?.paymentType?.let { payment ->
                    paymentChannelList.add(payment)
                }
            }
        }


        layoutSubHeader.visible()
        tvSubHeaderTitle.visible()
        tvSubHeaderTitle.text = resources.getString(R.string.settlement).uppercase()
        btnCancel.setOnClickListener { onBackPressed() }
        layout_back.setOnClickListener { onBackPressed() }
        btnOk.setOnClickListener {
            startSettlement()
        }

        btnOk.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        setThemePrimaryColor(btnOk)
        setThemeSecondaryColor(layoutBottom)

        queryAllSuspendedQR()
        disableAndHideCountDown()
    }

    private fun queryAllSuspendedQR() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.checkSuspendedQrLabel?.label))
            suspendedRepository.getAllSuspendedQr("PENDING").collect { it ->
                ProgressNotifier.getInstance().dismiss()
                val suspendedQrList = it?.getOrNull()
                if (suspendedQrList != null && suspendedQrList.isNotEmpty()) {
                    showDialog(msg = String.format(
                        StringUtils.getText(configModel.data?.stringFile?.messageSettlementSuspendedQrInquiryNowLabel?.label),
                        suspendedQrList.size.toString()
                    ),
                        actionConfirm = { inquirySuspended(suspendedQrList) },
                        actionCancel = { finish() })
                } else {
                    querySettlement()
                }

            }
        }
    }

    fun inquirySuspended(suspendedQrList: List<SuspendedQrDataModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ProgressNotifier.getInstance().show()
                ProgressNotifier.getInstance()
                    .primaryContent(StringUtils.getText(configModel.data?.stringFile?.inquirySuspendedQrLabel?.label))
                var totalPendingAfterInquiry = 0
                var isShowSuspendedAlertDialog = false
                val runningTasks = suspendedQrList.map {
                    async { // this will allow us to run multiple task
                        if (SystemParam.appIdOnline.get() == it.appid) {
                            ksherPay.UpdateAppId(
                                SystemParam.appIdOnline.get(), SystemParam.tokenOnline.decrypt()
                            )
                        } else {
                            ksherPay.UpdateAppId(
                                SystemParam.appIdOffline.get(), SystemParam.tokenOffline.decrypt()
                            )
                        }
                        val response = gatewayOrderQuery(it.mchOrderNo!!)
                        it to response
                    }
                }

                val responses = runningTasks.awaitAll()
                responses.forEach { (suspendedModel, response) ->
                    val jsonObject = JSONObject(response)
                    val dataObject = jsonObject.getJSONObject("data")
                    val result = dataObject.getString("result")
                    if ("SUCCESS".equals(result)) {
                        suspendedRepository.updateSuspendedQrWithTraceNo(
                            suspendedModel.traceNo.toString(), "SUCCESS"
                        )
                        val transData = TransDataModel.initFromSuspendedQr(suspendedModel)
                        setAmount(transData, dataObject)
                        setTimeToTransData(transData, dataObject)
                        setRefNoToTransData(transData, dataObject)
                        setTransactionId(transData, dataObject)
                        setInvoiceNo(transData, dataObject)
                        setAppId(transData, dataObject)

                        transactionRepository.insertTransaction(transData).collect {
                            // REMOVE ROW FROM SUSPENDED QR WHEN TRANSACTION SUCCESSFULLY.
                            suspendedRepository.updateSuspendedQrWithTraceNo(
                                traceNo = transData.traceNo.toString(), "SUCCESS"
                            ).collect()
                            SystemParam.incInvoiceNo()
                        }

                    } else if ("FAILURE".equals(result)) {
                        Timber.d("inquirySuspended FAIL " + response)
                        totalPendingAfterInquiry++
                        isShowSuspendedAlertDialog = true
                    } else if ("PENDING".equals(result)) {
                        Timber.d("inquirySuspended PENDING " + response)
                        totalPendingAfterInquiry++
                        isShowSuspendedAlertDialog = true
                    } else {
                        Timber.d("inquirySuspended ELSE " + response)
                        totalPendingAfterInquiry++
                        isShowSuspendedAlertDialog = true
                    }
                }



                if (isShowSuspendedAlertDialog) {
                    showDialog(msg = String.format(
                        StringUtils.getText(configModel.data?.stringFile?.messageSettlementSuspendedQrLabel?.label),
                        totalPendingAfterInquiry.toString()
                    ), actionConfirm = { querySettlement() }, actionCancel = { finish() })
                } else {
                    querySettlement()

                }
                ProgressNotifier.getInstance().dismiss()
            } catch (e: Exception) {

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
        val invoiceNo = transData.terminalId + DateUtils.getFormattedDate(
            timeEnd, "yyyy-MM-dd HH:mm:ss", "YYMMddHHmmss"
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
        return ksherPay.OrderQuery(mchOrderNo)
    }

    private fun startSettlement() {
        val channelSettlementJsonString = Gson().toJson(settlementModel)
        val grandTotalAmountJsonString = Gson().toJson(grandTotalAmount)
        val settlementData = SettlementDataModel.init().apply {
            terminalId = TerminalParam.number.get()
            merchantId = MerchantParam.merchantId.get()
            hostName = MerchantParam.name.get()
            channelDatas = channelSettlementJsonString
            grandTotalData = grandTotalAmountJsonString
        }
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.settlementStartLabel?.label))
            delay(2000)
            settlementRepository.insertSettlement(settlementData).collect {
                Timber.d("SettlementResult " + it)
                deleteCircular()
                //check result with initial transaction
                val isSuccess = reconcileSettlement(settlementData, it?.getOrNull())
                if (isSuccess) {
                    // increase batch no
                    SystemParam.incBatchNo()
                    ProgressNotifier.getInstance()
                        .showApprove(StringUtils.getText(configModel.data?.stringFile?.settlementSuccessLabel?.label))
                    delay(2000)
                    ProgressNotifier.getInstance().dismiss()
                    // print
                    ControllerParam.needPrintLastTrans.set(true)
                    Completable.fromAction {
                        ProgressNotifier.getInstance().show()
                    }.andThen(TransactionPrinting().printAnySettlement(settlementData))
                        .doOnComplete {
                            ControllerParam.needPrintLastTrans.set(false)
                            ProgressNotifier.getInstance().dismiss()
                            invokeMerchant(InvokeConstant.TRANSACTION_TYPE_SETTLEMENT, "0000", "success")
                            finish()
                        }.doFinally(ProgressNotifier.getInstance()::dismiss).doOnError { e ->
//                            isProcessDone = true
                            e.printStackTrace()
                            DeviceUtil.beepErr()
                            DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                        }.onErrorComplete().subscribe()
                } else {
                    settlementRepository.deleteLastSettlement()
                    ProgressNotifier.getInstance()
                        .showDecline(StringUtils.getText(configModel.data?.stringFile?.settlementFailLabel?.label))
                    ProgressNotifier.getInstance().show()
                    delay(2000)
                    showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.settlementFailLabel?.label),
                        actionCancel = { finish() },
                        actionConfirm = { startSettlement() })
                }
            }
        }
    }

    private suspend fun deleteCircular() {
        transactionRepository.deleteTransactionCircular(
            configModel.data?.setting?.systemMaxTransNumberDefault?.toInt() ?: 5000
        ).collect {}
    }

    private fun reconcileSettlement(
        settlementData: SettlementDataModel, settlementDataFromDB: SettlementDataModel?
    ) = settlementData == settlementDataFromDB

    private fun querySettlement() {
        settlementModel.settlements = ArrayList()
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance()
                .primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            paymentChannelList.forEach { paymentChannelName ->
                settlementRepository.getSettlementSaleAndRefundCountByChannel(paymentChannelName)
                    .collect {
                        Timber.d("SettlementItemModel : " + it?.getOrNull())
                        settlementModel.settlements.add((it?.getOrNull()
                            ?: SettlementItemModel()).apply {
                            paymentChannel = paymentChannelName
                        })
                    }
            }

            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                grandTotalAmount = (it?.getOrNull() ?: SettlementItemModel()).apply {
                    paymentChannel = "GRAND TOTAL"
                }
            }
            withContext(Dispatchers.Main) {
                setupDisplay()
                ProgressNotifier.getInstance().dismiss()
            }
        }
    }

    private fun setupDisplay() {
        tvTid.text = "TID: ${TerminalParam.number.get()}"
        tvMid.text = "MID: ${MerchantParam.merchantId.get()}"
        val year = DateUtils.getCurrentTime("yyyy")
        val date = DateUtils.getCurrentTime("MMdd")
        tvDate.text = DateUtils.getFormattedDate(
            year + date, "yyyyMMdd", "MMM dd, yy"
        )
        tvTime.text = DateUtils.getCurrentTime("HH:mm:ss")
        tvBatchNo.text = "BATCH: ${SystemParam.batchNo.get()}"
        tvHostName.text = "HOST NAME: ${MerchantParam.name.get()}"

        if (layoutContent.childCount > 0) layoutContent.removeAllViews()

        settlementModel.settlements.forEach {
            val view = LayoutInflater.from(this@SettlementActivity)
                .inflate(R.layout.settlement_detail_item, null)
            view.tvPaymentChannel.text = it.paymentChannel?.toPaymentChannelDisplay()
            view.tvSaleCount.text = it.saleCount.toString()
            view.tvSaleTotalAmount.text = it.saleTotalAmount.toAmount2DigitDisplay()

            view.tvRefundCount.text = it.refundCount.toString()
            view.tvRefundTotalAmount.text = it.refundTotalAmount.toAmount2DigitDisplay()
            layoutContent.addView(view)
        }

        val grandTotalView = LayoutInflater.from(this@SettlementActivity)
            .inflate(R.layout.settlement_grand_total_item, null)
        grandTotalView.tvPaymentChannel.text =
            grandTotalAmount.paymentChannel?.toPaymentChannelDisplay()
        grandTotalView.tvSaleCount.text = grandTotalAmount.saleCount.toString()
        grandTotalView.tvSaleTotalAmount.text =
            grandTotalAmount.saleTotalAmount.toAmount2DigitDisplay()
        grandTotalView.tvRefundCount.text = grandTotalAmount.refundCount.toString()
        grandTotalView.tvRefundTotalAmount.text =
            grandTotalAmount.refundTotalAmount.toAmount2DigitDisplay()
        layoutContent.addView(grandTotalView)

        if (grandTotalAmount.saleTotalAmount == 0 && grandTotalAmount.refundTotalAmount == 0) {
            showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                actionCancel = { finish() },
                actionConfirm = { finish() })
        } else {
            scrollView.visible()
            startCountDown()
        }
    }

    private fun invokeMerchant(
        transactionType: String?,
        respcode: String?,
        message: String?
    ) {
        val version: InvokeResponseHeaderData = InvokeResponseHeaderData().apply {
            version = BuildConfig.VERSION_NAME
        }
        val header: InvokeResponseDescriptionData = InvokeResponseDescriptionData().apply {
            this.respcode = respcode
            this.message = message
        }
        val response: InvokeResponseModel = InvokeResponseModel().apply {
            this.header = version
            this.data = header
        }
        val outputJson: String = Gson().toJson(response)

        if(SystemParam.isInvokeFlag.get() == true) {
            applicationContext.sendBroadcast(Intent().apply {
                SystemParam.isInvokeFlag.set(false)
                action = "com.evp.payment.invoke"
                putExtra("data_response", outputJson)
                putExtra("transaction_type", transactionType)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            })
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Activity?, keyAction: String, isShowInput: Boolean) {
            val intent = Intent(context, SettlementActivity::class.java)
            intent.putExtra("show_input_intent", isShowInput)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, keyAction: String, isShowInput: Boolean) {
            val intent = Intent(context, SettlementActivity::class.java)
            intent.putExtra("show_input_intent", isShowInput)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }
    }

}