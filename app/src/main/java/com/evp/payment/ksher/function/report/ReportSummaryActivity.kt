package com.evp.payment.ksher.function.report

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.SettlementItemModel
import com.evp.payment.ksher.database.SettlementModel
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_report_summary.*
import kotlinx.android.synthetic.main.activity_settlement.*
import kotlinx.android.synthetic.main.activity_settlement.btnCancel
import kotlinx.android.synthetic.main.activity_settlement.btnOk
import kotlinx.android.synthetic.main.activity_settlement.layoutBottom
import kotlinx.android.synthetic.main.activity_settlement.layoutContent
import kotlinx.android.synthetic.main.activity_settlement.scrollView
import kotlinx.android.synthetic.main.activity_settlement.tvBatchNo
import kotlinx.android.synthetic.main.activity_settlement.tvDate
import kotlinx.android.synthetic.main.activity_settlement.tvHostName
import kotlinx.android.synthetic.main.activity_settlement.tvMid
import kotlinx.android.synthetic.main.activity_settlement.tvTid
import kotlinx.android.synthetic.main.activity_settlement.tvTime
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ReportSummaryActivity : BaseTimeoutActivity() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivSubHeader: AppCompatImageView

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    private val payChannel by extra(("pay_channel"), "")
    private val payTitle by extra("pay_title", "")
    var settlementModel = SettlementModel()
    var grandTotalAmount = SettlementItemModel()
    private val paymentChannelList = listOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_report_summary

    override fun initViews() {
        layoutSubHeader.visible()
        tvHeaderTitle.visible()
        tvSubHeaderTitle.visible()
        ivSubHeader.visible()
        setupSubHeaderLogo(ivSubHeader, payChannel.orEmpty())

        setThemePrimaryColor(btnOk)
        setThemePrimaryColor(btnCancel)
        setThemeSecondaryColor(layoutBottom)

        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        btnOk.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)

        btnCancel.setOnClickListener { onBackPressed() }
        layout_back.setOnClickListener { onBackPressed() }
        btnOk.setOnClickListener {
            printSummaryReport()
        }
        if(payChannel?.length!! > 0){
            tvHeaderTitle.text = StringUtils.getText(configModel.data?.button?.buttonSummaryReport?.label)
            querySummaryReportByChannel()
        }else {
            tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.button?.buttonAllPayment?.label)
            tvHeaderTitle.text = payTitle
            querySummaryReport()
        }
    }

    private fun printSummaryReport() {
        stopCountDown()
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
                // print
            Completable.fromAction {
                ProgressNotifier.getInstance().show()
            }
                .andThen(TransactionPrinting().printAnySummaryReport(settlementData))
                .doOnComplete {
                    ProgressNotifier.getInstance().dismiss()
                    finish()
                }
                .doFinally(ProgressNotifier.getInstance()::dismiss)
                .doOnError { e ->
                    e.printStackTrace()
                    DeviceUtil.beepErr()
                    DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                }
                .onErrorComplete()
                .subscribe()
        }
    }

    private fun querySummaryReport(){
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            paymentChannelList.forEach { paymentChannelName ->
                transactionRepository.getSettlementSaleAndRefundCountByChannel(paymentChannelName).collect {
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

    private fun querySummaryReportByChannel(){
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
                transactionRepository.getSettlementSaleAndRefundCountByChannel(payChannel!!).collect {
                    settlementModel.settlements.add((it?.getOrNull()
                        ?: SettlementItemModel()).apply {
                        paymentChannel = payChannel!!
                    })
                }

            settlementRepository.getSettlementSaleAndRefundCountByChannel(payChannel!!).collect {
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
            year + date,
            "yyyyMMdd",
            "MMM dd, yy"
        )
        tvTime.text = DateUtils.getCurrentTime("HH:mm:ss")
        tvBatchNo.text = "BATCH: ${SystemParam.batchNo.get()}"
        tvHostName.text = "HOST NAME: ${MerchantParam.name.get()}"

        if (layoutContent.childCount > 0)
            layoutContent.removeAllViews()

        settlementModel.settlements.forEach {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.settlement_detail_item, null)
            view.tvPaymentChannel.text = it.paymentChannel?.toPaymentChannelDisplay()
            view.tvSaleCount.text = it.saleCount.toString()
            view.tvSaleTotalAmount.text = it.saleTotalAmount.toAmountDisplay()

            view.tvRefundCount.text = it.refundCount.toString()
            view.tvRefundTotalAmount.text = it.refundTotalAmount.toAmountDisplay()
            layoutContent.addView(view)
        }

        val grandTotalView = LayoutInflater.from(this)
            .inflate(R.layout.settlement_grand_total_item, null)
        grandTotalView.tvPaymentChannel.text =
            grandTotalAmount.paymentChannel?.toPaymentChannelDisplay()
        grandTotalView.tvSaleCount.text = grandTotalAmount.saleCount.toString()
        grandTotalView.tvSaleTotalAmount.text = grandTotalAmount.saleTotalAmount.toAmount2DigitDisplay()
        grandTotalView.tvRefundCount.text = grandTotalAmount.refundCount.toString()
        grandTotalView.tvRefundTotalAmount.text =
            grandTotalAmount.refundTotalAmount.toAmount2DigitDisplay()
        layoutContent.addView(grandTotalView)

        if (settlementModel.settlements.size == 0) {
            showDialog(
                msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                actionCancel = { finish() },
                actionConfirm = { finish() }
            )
        } else {
            scrollView.visible()
        }
    }
}