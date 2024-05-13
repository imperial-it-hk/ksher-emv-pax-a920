package com.evp.payment.ksher.function.report

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.parameter.TerminalParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_report_audit.*
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
import kotlinx.android.synthetic.main.audit_grand_total_item.view.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.report_audit_detail_item.view.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvSaleCount
import kotlinx.android.synthetic.main.settlement_detail_item.view.tvSaleTotalAmount
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class ReportAuditActivity : BaseTimeoutActivity() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

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
    private val paymentChannelList =
        listOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")
    var transDataModel: ArrayList<HistoryData> = arrayListOf()
    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_report_audit

    override fun initViews() {
        layoutSubHeader.visible()
        tvHeaderTitle.visible()
        tvSubHeaderTitle.visible()
        ivSubHeader.visible()

        setThemePrimaryColor(btnOk)
        setThemePrimaryColor(btnCancel)
        setThemeSecondaryColor(layoutBottom)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        btnOk.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)
        setupSubHeaderLogo(ivSubHeader, payChannel.orEmpty())
        btnCancel.setOnClickListener { onBackPressed() }
        layout_back.setOnClickListener { onBackPressed() }
        btnOk.setOnClickListener {
            printAllAuditReport()
        }
        if (payChannel?.length!! > 0) {
            tvHeaderTitle.text = StringUtils.getText(configModel.data?.button?.buttonAuditReport?.label)
            queryAuditReportByChannel()
        } else {
            tvHeaderTitle.text = payTitle
            tvSubHeaderTitle.text = StringUtils.getText(configModel.data?.button?.buttonAllPayment?.label)
            queryAuditReport()
        }
    }

    private fun printAllAuditReport() {
        stopCountDown()
        CoroutineScope(Dispatchers.IO).launch {
            // print
            Completable.fromAction {
                ProgressNotifier.getInstance().show()
            }
                .andThen(TransactionPrinting().printAnyAuditReport(transDataModel))
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

    private fun queryAuditReport() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            try {
                transactionRepository.getReportAuditAllChannel().collect {
                    transDataModel = arrayListOf()
                    transDataModel.addAll(it?.getOrNull()?.results!!)

                    withContext(Dispatchers.Main) {
                        setupDisplay(it.getOrNull()?.results!!)
                        ProgressNotifier.getInstance().dismiss()
                    }

                    var totalCount = 0
                    var totalAmount = 0
                    paymentChannelList.forEach { paymentChannelName ->
                        transactionRepository.getSaleCountByChanel(paymentChannelName)
                            .collect {
                                withContext(Dispatchers.Main) {
                                    transDataModel.add(it!!.getOrDefault(
                                        SaleTotalByChannelModel(
                                            paymentChannelName,
                                            0,
                                            0
                                        )
                                    ))
                                    totalCount += it.getOrNull()?.saleCount ?: 0
                                    totalAmount += it.getOrNull()?.saleTotalAmount ?: 0
                                    setupDisplaySaleCount(
                                        it.getOrDefault(
                                            SaleTotalByChannelModel(
                                                paymentChannelName,
                                                0,
                                                0
                                            )
                                        )
                                    )
                                    ProgressNotifier.getInstance().dismiss()
                                }
                            }
                    }
                    withContext(Dispatchers.Main) {
                        val separteView = LayoutInflater.from(this@ReportAuditActivity)
                            .inflate(R.layout.item_separate, null)
                        layoutContent.addView(separteView)

                        val grandTotalView = LayoutInflater.from(this@ReportAuditActivity)
                            .inflate(R.layout.audit_grand_total_item, null)
                        grandTotalView.tvSaleCount.text = totalCount.toString()
                        grandTotalView.tvSaleTotalAmount.text =
                            totalAmount.toString().toAmount2DigitDisplay()
                        layoutContent.addView(grandTotalView)
                    }
                }
            } catch (e: Exception) {
                ProgressNotifier.getInstance().dismiss()
                showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                    actionCancel = { finish() },
                    actionConfirm = { finish() })
            }

        }
    }

    private fun setupDisplaySaleCount(item: SaleTotalByChannelModel) {
        val grandTotalView = LayoutInflater.from(this@ReportAuditActivity)
            .inflate(R.layout.audit_channel_total_item, null)
        grandTotalView.tvPaymentChannel.text =
            item.paymentChannel?.toPaymentChannelDisplay()
        grandTotalView.tvSaleCount.text = item.saleCount.toString()
        grandTotalView.tvSaleTotalAmount.text =
            item.saleTotalAmount.toAmount2DigitDisplay()

        layoutContent.addView(grandTotalView)
    }

    private fun queryAuditReportByChannel() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            try {
                transactionRepository.getReportAuditByChannel(payChannel.orEmpty()).collect {
                    transDataModel = arrayListOf()
                    transDataModel.addAll(it?.getOrNull()?.results!!)

                    withContext(Dispatchers.Main) {
                        setupDisplay(it.getOrNull()?.results.orEmpty())
                        ProgressNotifier.getInstance().dismiss()
                    }

                    var totalCount = 0
                    var totalAmount = 0

                    transactionRepository.getSaleCountByChanel(payChannel!!)
                        .collect {
                            withContext(Dispatchers.Main) {
                                transDataModel.add(it!!.getOrDefault(
                                    SaleTotalByChannelModel(
                                        payChannel,
                                        0,
                                        0
                                    )
                                ))
                                totalCount += it.getOrNull()?.saleCount ?: 0
                                totalAmount += it.getOrNull()?.saleTotalAmount ?: 0
                                setupDisplaySaleCount(
                                    it.getOrDefault(
                                        SaleTotalByChannelModel(
                                            payChannel,
                                            0,
                                            0
                                        )
                                    )
                                )
                                ProgressNotifier.getInstance().dismiss()
                            }
                        }

                    withContext(Dispatchers.Main) {
                        val separteView = LayoutInflater.from(this@ReportAuditActivity)
                            .inflate(R.layout.item_separate, null)
                        layoutContent.addView(separteView)

                        val grandTotalView = LayoutInflater.from(this@ReportAuditActivity)
                            .inflate(R.layout.audit_grand_total_item, null)
                        grandTotalView.tvSaleCount.text = totalCount.toString()
                        grandTotalView.tvSaleTotalAmount.text =
                            totalAmount.toString().toAmount2DigitDisplay()
                        layoutContent.addView(grandTotalView)
                    }
                }
            } catch (e: Exception) {
                ProgressNotifier.getInstance().dismiss()
                showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                    actionCancel = { finish() },
                    actionConfirm = { finish() })
            }

        }
    }

    private fun setupDisplay(transDataModel: List<TransDataModel>) {
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

        transDataModel.forEach {
            val view = LayoutInflater.from(this).inflate(R.layout.report_audit_detail_item, null)
            view.tvPaymentType.text =
                it.paymentChannel?.toPaymentChannelDisplay() + " Pay " + it.transType

            if (it.transType == ETransType.VOID.toString() || it.transType == ETransType.REFUND.toString()) {
                view.tvAmount.text = "THB -${it.amount?.toAmount2DigitDisplay()}"
            } else {
                view.tvAmount.text = "THB ${it.amount?.toAmount2DigitDisplay()}"
            }


            view.tvDateDetail.text = DateUtils.getFormattedDate(
                "${it.year + it.date} ${it.time}",
                "yyyyMMdd HHmmss",
                "MMM dd, yy"
            )
            view.tvDateTimeDetail.text = DateUtils.getFormattedDate(
                "${it.year + it.date} ${it.time}",
                "yyyyMMdd HHmmss",
                "HH:mm:ss"
            )
            view.tvTransactionId.text = it.transactionId.toString()
            view.tvInvoiceNo.text = it.invoiceNo.toString()

            layoutContent.addView(view)
        }

        val separteView = LayoutInflater.from(this@ReportAuditActivity)
            .inflate(R.layout.item_separate, null)
        layoutContent.addView(separteView)


        if (transDataModel.isEmpty()) {
            showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                actionCancel = { finish() },
                actionConfirm = { finish() })
        } else {
            scrollView.visible()
        }
    }
}