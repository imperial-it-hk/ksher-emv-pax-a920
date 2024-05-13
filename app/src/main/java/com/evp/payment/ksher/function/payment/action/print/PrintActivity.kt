package com.evp.payment.ksher.function.payment.action.print

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.*
import com.evp.payment.ksher.function.config.ConfigActivity
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.invoke.InvokeResponseDescriptionData
import com.evp.payment.ksher.invoke.InvokeResponseHeaderData
import com.evp.payment.ksher.invoke.InvokeResponseModel
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.CurrencyParam.currency
import com.evp.payment.ksher.parameter.MerchantParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.transactions.ETransType
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import io.reactivex.Completable
import kotlinx.android.synthetic.main.activity_print_layout.*
import java.math.BigDecimal

class PrintActivity : BaseTimeoutActivity() {

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: LinearLayout

    @BindView(R.id.layout_back)
    lateinit var layoutBack: View

    @BindView(R.id.header_title)
    lateinit var headerTitle: View

    @BindView(R.id.layout_slip)
    lateinit var layoutSlip: View

    @BindView(R.id.btn_cancel)
    lateinit var btnCancel: Button

    @BindView(R.id.btn_print)
    lateinit var btnPrint: Button

    @BindView(R.id.tvTid)
    lateinit var tvTid: TextView

    @BindView(R.id.tvMid)
    lateinit var tvMid: TextView

    @BindView(R.id.tvTraceNo)
    lateinit var tvTraceNo: TextView

    @BindView(R.id.tvBatchNo)
    lateinit var tvBatchNo: TextView

    @BindView(R.id.tvDate)
    lateinit var tvDate: TextView

    @BindView(R.id.tvTime)
    lateinit var tvTime: TextView

    @BindView(R.id.tvApproveCodeTitle)
    lateinit var tvApproveCodeTitle: TextView

    @BindView(R.id.tvApproveCode)
    lateinit var tvApproveCode: TextView

    @BindView(R.id.tvPrintTitle)
    lateinit var tvPrintTitle: TextView

    @BindView(R.id.tvRefNo)
    lateinit var tvRefNo: TextView

    @BindView(R.id.tvTransactionType)
    lateinit var tvTransactionType: TextView

    @BindView(R.id.tvPaymentType)
    lateinit var tvPaymentType: TextView

    @BindView(R.id.tvTransactionId)
    lateinit var tvTransactionId: TextView

    @BindView(R.id.tvInvoiceNo)
    lateinit var tvInvoiceNo: TextView

    @BindView(R.id.tvAmount)
    lateinit var tvAmount: TextView

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubTitle: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var tvTitleImage: AppCompatImageView

    private val transData by extraNotNull<TransDataModel>("transData")
    private val isRePrint by extra("re_print", false)
    private val slipCount by extra("slipCount", 1)
    private val payChannel by extra(("pay_channel"), "")
    private val payTitle by extra("pay_title", "")
    private val paySubTitle by extra("pay_sub_title", "")
    private val mediaType by extra<String>("media_type", "")

    override fun loadParam() {

    }

    override val layoutId: Int
        get() = R.layout.activity_print_layout

    override fun initViews() {
        layoutBack.invisible()
        headerTitle.visible()
        layoutSubHeader.visible()

        btnPrint.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)


        setThemePrimaryColor(btnPrint)

        if (!payTitle.isNullOrEmpty()) {
            tvTitle.visible()
            tvTitle.text = payTitle
        }else{
            tvTitle.visible()
            tvTitle.text = StringUtils.getText(configModel.data?.stringFile?.printLabel?.label)
        }

        if (!payChannel.isNullOrEmpty()) {
            tvTitleImage.visible()
            setupSubHeaderLogo(tvTitleImage, payChannel.orEmpty())
        }

        if (!paySubTitle.isNullOrEmpty()) {
            tvSubTitle.visible()
            tvSubTitle.text = paySubTitle
        }

        setupDisplay()


        layoutSlip.isDrawingCacheEnabled = true

        btnCancel.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            })
            invokeMerchant(transData, mediaType, transData.transType, "0000", "success")
            finish()
            super.onBackPressed()
        }
        btnPrint.setOnClickListener {
            disableCountDown()
            ControllerParam.needPrintLastTrans.set(true)
            if (isRePrint == false) {
                Completable.fromAction(ProgressNotifier.getInstance()::show)
                    .andThen(TransactionPrinting().printAnyTrans(transData, slipCount ?: 1))
                    .doOnComplete {
                        ControllerParam.needPrintLastTrans.set(false)
                        if (slipCount == 0) {
                            val bundle = Bundle()
                            val intent = Intent(this, PrintActivity::class.java)
                            intent.putExtra("pay_title", payTitle)
                            intent.putExtra("pay_sub_title", paySubTitle)
                            bundle.putParcelable("transData", transData)
                            intent.putExtra("re_print", isRePrint)
                            intent.putExtra("slipCount", 1)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            finish()
                        } else {
                            setResult(RESULT_OK, Intent().apply {
                                addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                            })
                            invokeMerchant(
                                transData,
                                mediaType,
                                transData.transType,
                                "0000",
                                "success"
                            )
                            finish()
                        }
                    }.doFinally(ProgressNotifier.getInstance()::dismiss).doOnError { e ->
                        DeviceUtil.beepErr()
                        DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                    }.onErrorComplete().subscribe()
            } else {
                Completable.fromAction(ProgressNotifier.getInstance()::show)
                    .andThen(TransactionPrinting(true).printAnyTrans(transData, slipCount ?: 1))
                    .doOnComplete {
                        ControllerParam.needPrintLastTrans.set(false)
                        if (slipCount == 0) {
                            val bundle = Bundle()
                            val intent = Intent(this, PrintActivity::class.java)
                            intent.putExtra("pay_title", payTitle)
                            intent.putExtra("pay_sub_title", paySubTitle)
                            bundle.putParcelable("transData", transData)
                            intent.putExtra("re_print", isRePrint)
                            intent.putExtra("slipCount", 1)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            finish()
                        } else {
                            setResult(RESULT_OK, Intent().apply {
                                addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                            })
                            invokeMerchant(
                                transData,
                                mediaType,
                                transData.transType,
                                "0000",
                                "success"
                            )
                            finish()
                        }
                    }.doFinally(ProgressNotifier.getInstance()::dismiss).doOnError { e ->
                        DeviceUtil.beepErr()
                        DialogUtils.showAlertTimeout(e.message, DialogUtils.TIMEOUT_FAIL)
                    }.onErrorComplete().subscribe()
            }
        }
    }

    private fun setupDisplay() {
        tvApproveCodeTitle.gone()
        tvApproveCode.gone()

        if (slipCount == 0) {
            btnPrint.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
            tvPrintTitle.text = StringUtils.getText(configModel.data?.stringFile?.pressOkToPrintSlipLabel?.label)
        } else {
            btnPrint.text = StringUtils.getText(configModel.data?.button?.buttonPrint?.label)
            tvPrintTitle.text = StringUtils.getText(configModel.data?.stringFile?.printCustomerCopyLabel?.label)
        }

        transData.let { td ->
            if (td.terminalId.isNullOrEmpty()) {
                tvTid.text = "TID:"
            } else {
                tvTid.text = "TID: ${td.terminalId!!}"
            }

            if (td.merchantId.isNullOrEmpty()) {
                tvMid.text = MerchantParam.merchantId.get()
            } else {
                tvMid.text = "MID: ${td.merchantId!!}"
            }

            tvBatchNo.text = "BATCH: ${String.format("%06d", td.batchNo!!)}"
            tvDate.text = DateUtils.getFormattedDate(
                "${td.year + td.date} ${td.time}", "yyyyMMdd HHmmss", "MMM dd, yy"
            )
            tvTime.text = DateUtils.getFormattedDate(
                "${td.year + td.date} ${td.time}", "yyyyMMdd HHmmss", "HH:mm:ss"
            )


//            if (td.authCode.isNullOrEmpty()) {
//                tvApproveCode.gone()
//            } else {
//                tvApproveCode.text = td.authCode!!
//            }

            if (td.referNo.isNullOrEmpty()) {
                tvRefNo.gone()
            } else {
                tvRefNo.text = td.referNo!!
            }

            tvTransactionType.text = td.transType

            tvPaymentType.text = td.paymentChannel?.toPaymentChannelDisplay()

            tvTransactionId.text = td.transactionId
            tvInvoiceNo.text = td.invoiceNo.toString()

            if (BigDecimal(transData.exchangeRate).compareTo(BigDecimal.ONE) != 0) {
                layoutConvert.visible()
                layoutRate.visible()
                tvAmountLabel.text = "${currency.getName()} AMT"
                tvRate.text = td.exchangeRate
                tvRateLabel.text = "Ex. RATE(${transData.currencyConvert}/${currency.getName()})"
                tvConvertLabel.text = "${transData.currencyConvert} AMT"

                if (td.transType == ETransType.VOID.toString() || td.transType == ETransType.REFUND.toString()) {
                    tvTraceNo.text = "TRACE: ${String.format("%06d", td.origTraceNo!!)}"
                    tvAmount.text = "-${td.amount?.toAmount2DigitDisplay()}"
                    tvConvert.text = "-${td.amountConvert?.toAmount2DigitDisplay()}"
                } else {
                    tvTraceNo.text = "TRACE: ${String.format("%06d", td.traceNo!!)}"
                    tvAmount.text = "${td.amount?.toAmount2DigitDisplay()}"
                    tvConvert.text = "${td.amountConvert?.toAmount2DigitDisplay()}"
                }
            } else {
                layoutConvert.gone()
                layoutRate.gone()
                tvAmountLabel.text = "AMT"

                if (td.transType == ETransType.VOID.toString() || td.transType == ETransType.REFUND.toString()) {
                    tvTraceNo.text = "TRACE: ${String.format("%06d", td.origTraceNo!!)}"
                    tvAmount.text = "${currency.getName()} -${td.amount?.toAmount2DigitDisplay()}"
                } else {
                    tvTraceNo.text = "TRACE: ${String.format("%06d", td.traceNo!!)}"
                    tvAmount.text = "${currency.getName()} ${td.amount?.toAmount2DigitDisplay()}"
                }
            }
        }
    }

    override fun onBackPressed() {
    }

    private fun invokeMerchant(
        transData: TransDataModel,
        transactionType: String?,
        mediaType: String?,
        respcode: String?,
        message: String?
    ) {
        val version: InvokeResponseHeaderData = InvokeResponseHeaderData().apply {
            version = BuildConfig.VERSION_NAME
        }
        val header: InvokeResponseDescriptionData = InvokeResponseDescriptionData().apply {
            this.respcode = respcode
            this.message = message
            this.description = transData
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
                putExtra("media_type", mediaType)
                putExtra("transaction_type", transactionType)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            })
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?, transData: TransDataModel?) {
            val bundle = Bundle()
            val intent = Intent(context, PrintActivity::class.java)
            bundle.putParcelable("transData", transData)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, mediaType: String?, transData: TransDataModel?) {
            val bundle = Bundle()
            val intent = Intent(context, PrintActivity::class.java)
            bundle.putParcelable("transData", transData)
            intent.putExtra("media_type", mediaType)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }
}