package com.evp.payment.ksher.function.print

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import butterknife.BindView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.repository.SettlementRepository
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
import kotlinx.android.synthetic.main.activity_print_settlement.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.settlement_detail_item.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class PrintLastSettlementActivity : BaseTimeoutActivity() {

    @Inject
    lateinit var settlementRepository: SettlementRepository

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: View

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubHeaderTitle: TextView

    private val payTitle by extra("pay_title", "")
    private val paySubTitle by extra("pay_sub_title", "")

    val paymentChannelList =
        listOf("linepay", "promptpay", "truemoney", "alipay", "wechat", "airpay")

    var settlementDataModel = SettlementDataModel()
    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_print_settlement

    override fun initViews() {
        layout_sub_header.visible()
        tv_sub_header_title.visible()
        tv_sub_header_detail.visible()
        tv_sub_header_title.text = payTitle ?: StringUtils.getText(configModel.data?.stringFile?.settlementLabel?.label)
        tv_sub_header_detail.text = paySubTitle
        btnCancel.setOnClickListener { onBackPressed() }
        layout_back.setOnClickListener { onBackPressed() }
        btnOk.setOnClickListener {
            printLastSettlement()
        }
        queryLastSettlement()
        btnOk.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        setThemePrimaryColor(btnOk)
        setThemePrimaryColor(btnCancel)
        setThemeSecondaryColor(layoutBottom)

    }

    private fun printLastSettlement() {
        stopCountDown()
        CoroutineScope(Dispatchers.IO).launch {
            Completable.fromAction {
                ProgressNotifier.getInstance().show()
            }.andThen(TransactionPrinting(true).printAnySettlement(settlementDataModel)).doOnComplete {
                    ProgressNotifier.getInstance().dismiss()
                    finish()
                }.doFinally(ProgressNotifier.getInstance()::dismiss).doOnError { e ->
                    e.printStackTrace()
                    DeviceUtil.beepErr()
                    DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                }.onErrorComplete().subscribe()
        }
    }

    private fun queryLastSettlement() {
        CoroutineScope(Dispatchers.IO).launch {
            ProgressNotifier.getInstance().show()
            ProgressNotifier.getInstance().primaryContent(StringUtils.getText(configModel.data?.stringFile?.queryTransactionLabel?.label))
            try {
                settlementRepository.getLastSettlement().collect {
                    settlementDataModel = it?.getOrNull()!!
                    val channelData: SettlementModel = Gson().fromJson(
                        it.getOrNull()?.channelDatas, SettlementModel::class.java
                    )
                    val grandTotalData: SettlementItemModel = Gson().fromJson(
                        it.getOrNull()?.grandTotalData, SettlementItemModel::class.java
                    )

                    withContext(Dispatchers.Main) {
                        setupDisplay(channelData, grandTotalData)
                        ProgressNotifier.getInstance().dismiss()
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

    private fun setupDisplay(
        settlementModel: SettlementModel,
        grandTotalAmount: SettlementItemModel
    ) {
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
            val view = LayoutInflater.from(this@PrintLastSettlementActivity)
                .inflate(R.layout.settlement_detail_item, null)
            view.tvPaymentChannel.text = it.paymentChannel?.toPaymentChannelDisplay()
            view.tvSaleCount.text = it.saleCount.toString()
            view.tvSaleTotalAmount.text = it.saleTotalAmount.toAmountDisplay()

            view.tvRefundCount.text = it.refundCount.toString()
            view.tvRefundTotalAmount.text = it.refundTotalAmount.toAmountDisplay()
            layoutContent.addView(view)
        }

        val grandTotalView = LayoutInflater.from(this@PrintLastSettlementActivity)
            .inflate(R.layout.settlement_grand_total_item, null)

        grandTotalView.tvPaymentChannel.text =
            grandTotalAmount.paymentChannel?.toPaymentChannelDisplay()
        grandTotalView.tvSaleCount.text = grandTotalAmount.saleCount.toString()
        grandTotalView.tvSaleTotalAmount.text = grandTotalAmount.saleTotalAmount.toAmountDisplay()
        grandTotalView.tvRefundCount.text = grandTotalAmount.refundCount.toString()
        grandTotalView.tvRefundTotalAmount.text =
            grandTotalAmount.refundTotalAmount.toAmountDisplay()
        layoutContent.addView(grandTotalView)

        if (grandTotalAmount.saleTotalAmount == 0 && grandTotalAmount.refundTotalAmount == 0) {
            showDialog(msg = StringUtils.getText(configModel.data?.stringFile?.noTransactionFoundLabel?.label),
                actionCancel = { finish() },
                actionConfirm = { finish() })
        } else {
            scrollView.visible()
        }
    }
}