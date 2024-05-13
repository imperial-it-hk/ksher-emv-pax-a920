package com.evp.payment.ksher.function.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.repository.SuspendedRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.extension.extra

import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.history.TransactionHistoryActivity
import com.evp.payment.ksher.function.inquiry.InquiryInputTransactionActivity
import com.evp.payment.ksher.function.inquiry.suspendedqr.SuspendedQrActivity
import com.evp.payment.ksher.function.main.view.PaymentActionTypeContact
import com.evp.payment.ksher.function.payment.PaymentSelectorActivity
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.print.PrintInputTransactionActivity
import com.evp.payment.ksher.function.print.PrintLastSettlementActivity
import com.evp.payment.ksher.function.report.ReportAuditActivity
import com.evp.payment.ksher.function.report.ReportSummaryActivity
import com.evp.payment.ksher.function.settlement.SettlementActivity
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class PaymentActionTypeActivity : BaseTimeoutActivity(), PaymentActionTypeContact.View {

    private val payTitle by extra("pay_title", "")
    var nextTitle: String? = null
    var nextSubTitle = ""

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository

    private val presenter: PaymentActionTypePresenter by lazy {
        PaymentActionTypePresenter(
            this,
            transactionRepository,
            suspendedRepository,
            configModel
        )
    }

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvDetailTitle: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivSubHeader: AppCompatImageView

    ///////////////// Print //////////////////
    @BindView(R.id.bt_last_transaction_print)
    lateinit var btLastTransactionPrint: AppCompatButton

    @BindView(R.id.bt_any_transaction_print)
    lateinit var btAnyTransactionPrint: AppCompatButton

    @BindView(R.id.bt_last_settlement_print)
    lateinit var btLastSettlementPrint: AppCompatButton

    ///////////////// Query //////////////////
    @BindView(R.id.bt_last_transaction_inquiry)
    lateinit var btLastTransactionInquiry: AppCompatButton

    @BindView(R.id.bt_suspended_qr_inquiry)
    lateinit var btSuspendedQrInquiry: AppCompatButton

    @BindView(R.id.bt_any_transaction_inquiry)
    lateinit var btAnyTransactionInquiry: AppCompatButton

    ///////////////// Report //////////////////
    @BindView(R.id.bt_summary_report)
    lateinit var btSummaryReport: AppCompatButton

    @BindView(R.id.bt_audit_report)
    lateinit var btAuditReport: AppCompatButton

    @BindView(R.id.bt_all_payment_report)
    lateinit var btAllPaymentReport: AppCompatButton

    @BindView(R.id.bt_select_payment_report)
    lateinit var btSelectPaymentReport: AppCompatButton

    @BindView(R.id.bt_all_payment_report_audit)
    lateinit var btAllPaymentReportAudit: AppCompatButton

    @BindView(R.id.bt_select_payment_report_audit)
    lateinit var btSelectReportAudit: AppCompatButton

    ///////////////// Settlement //////////////////
    @BindView(R.id.bt_all_payment_settlement)
    lateinit var btAllPaymentSettlement: AppCompatButton

    @BindView(R.id.bt_select_payment_settlement)
    lateinit var btSelectPaymentSettlement: AppCompatButton

    private lateinit var ACTION_METHOD_TYPE: String

    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_action_type

    override fun initViews() {
        layoutTitle.visibility = View.VISIBLE
        tvHeaderTitle.visibility = View.VISIBLE
        tvHeaderTitle.text = payTitle
        ivSubHeader.visibility = View.VISIBLE

        btLastTransactionPrint.text = StringUtils.getText(configModel.data?.button?.buttonLastTransaction?.label)
        btAnyTransactionPrint.text = StringUtils.getText(configModel.data?.button?.buttonAnyTransaction?.label)
        btLastSettlementPrint.text = StringUtils.getText(configModel.data?.button?.buttonLastSettlement?.label)

        setThemePrimaryColor(btLastTransactionPrint)
        setThemePrimaryColor(btAnyTransactionPrint)
        setThemePrimaryColor(btLastSettlementPrint)

        ///////////////// Query //////////////////
        btLastTransactionInquiry.text = StringUtils.getText(configModel.data?.button?.buttonLastTransaction?.label)
        btSuspendedQrInquiry.text = StringUtils.getText(configModel.data?.button?.buttonSuspendedQR?.label)
        btAnyTransactionInquiry.text = StringUtils.getText(configModel.data?.button?.buttonAnyTransaction?.label)

        setThemePrimaryColor(btLastTransactionInquiry)
        setThemePrimaryColor(btSuspendedQrInquiry)
        setThemePrimaryColor(btAnyTransactionInquiry)

        ///////////////// Report //////////////////
        btSummaryReport.text = StringUtils.getText(configModel.data?.button?.buttonSummaryReport?.label)
        btAuditReport.text = StringUtils.getText(configModel.data?.button?.buttonAuditReport?.label)
        btAllPaymentReport.text = StringUtils.getText(configModel.data?.button?.buttonAllPayment?.label)
        btSelectPaymentReport.text = StringUtils.getText(configModel.data?.button?.buttonSelectPayment?.label)
        btAllPaymentReportAudit.text = StringUtils.getText(configModel.data?.button?.buttonAllPayment?.label)
        btSelectReportAudit.text = StringUtils.getText(configModel.data?.button?.buttonSelectPayment?.label)

        setThemePrimaryColor(btSummaryReport)
        setThemePrimaryColor(btAuditReport)
        setThemePrimaryColor(btAllPaymentReport)
        setThemePrimaryColor(btSelectPaymentReport)
        setThemePrimaryColor(btAllPaymentReportAudit)
        setThemePrimaryColor(btSelectReportAudit)

        ///////////////// Settlement //////////////////
        btAllPaymentSettlement.text = StringUtils.getText(configModel.data?.button?.buttonAllPayment?.label)
        btSelectPaymentSettlement.text = StringUtils.getText(configModel.data?.button?.buttonSelectPayment?.label)

        setThemePrimaryColor(btAllPaymentSettlement)
        setThemePrimaryColor(btSelectPaymentSettlement)

        try {
            if (intent.hasExtra(PaymentAction.KEY_ACTION)) {
                ACTION_METHOD_TYPE = intent.getStringExtra(PaymentAction.KEY_ACTION)!!
            }

            when (ACTION_METHOD_TYPE) {
                PaymentAction.PRINT -> {
                    tvDetailTitle.visibility = View.VISIBLE
                    tvDetailTitle.text = StringUtils.getText(configModel.data?.stringFile?.selectChoiceToPrintLabel?.label)
                    btLastTransactionPrint.visibility = View.VISIBLE
                    btAnyTransactionPrint.visibility = View.VISIBLE
                    btLastSettlementPrint.visibility = View.VISIBLE
                }
                PaymentAction.QUERY -> {
//                    tvDetailTitle.text = "QR Inquiry"
                    btLastTransactionInquiry.visibility = View.VISIBLE
                    btSuspendedQrInquiry.visibility = View.VISIBLE
                    btAnyTransactionInquiry.visibility = View.VISIBLE
                }
                PaymentAction.REPORT -> {
//                    tvDetailTitle.text = "Select Report Type"
                    btSummaryReport.visibility = View.VISIBLE
                    btAuditReport.visibility = View.VISIBLE
                }
                PaymentAction.SETTLEMENT -> {
                    tvDetailTitle.visibility = View.VISIBLE
                    tvDetailTitle.text = StringUtils.getText(configModel.data?.stringFile?.settlementLabel?.label)
                    btAllPaymentSettlement.visibility = View.VISIBLE
                    btSelectPaymentSettlement.visibility = View.VISIBLE
                }
                PaymentAction.GENERATOR_QR -> {

                }
            }
        } catch (e: Exception) {

        }

    }

    protected fun startActivity(cls: Class<*>?, bundle: Bundle) {
        val context: Context = BaseApplication.appContext!!
        val intent = Intent(context, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }


    ///////////////// Print //////////////////
    @OnClick(R.id.bt_last_transaction_print)
    override fun lastTransactionPrint() {
        nextTitle =  StringUtils.getText(configModel.data?.stringFile?.printLabel?.label)
        nextSubTitle = StringUtils.getText(configModel.data?.stringFile?.lastTransactionLabel?.label)
        presenter.printLastTransaction()
    }

    @OnClick(R.id.bt_any_transaction_print)
    override fun anyTransactionPrint() {
        val intent = Intent(this, PrintInputTransactionActivity::class.java)
//        intent.putExtra("pay_channel", payChannel)
        intent.putExtra("pay_title", payTitle)
        intent.putExtra("pay_sub_title", StringUtils.getText(configModel.data?.stringFile?.anyTransactionLabel?.label))
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.bt_last_settlement_print)
    override fun lastSettlementPrint() {
        val intent = Intent(this, PrintLastSettlementActivity::class.java)
        intent.putExtra("pay_title", StringUtils.getText(configModel.data?.stringFile?.printLabel?.label))
        intent.putExtra("pay_sub_title", btLastSettlementPrint.text.toString())
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.PRINT)
        startActivity(intent)
        finish()
    }

    ///////////////// Query //////////////////
    @OnClick(R.id.bt_last_transaction_inquiry)
    override fun lastTransactionInquiry() {
        nextTitle = StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label)
        nextSubTitle = StringUtils.getText(configModel.data?.stringFile?.lastTransactionLabel?.label)
        presenter.inquiryLastTransaction()
    }

    @OnClick(R.id.bt_suspended_qr_inquiry)
    override fun suspendedQrInquiry() {
        val intent = Intent(this, SuspendedQrActivity::class.java)
        intent.putExtra("pay_title", payTitle)
        startActivity(intent)
    }

    @OnClick(R.id.bt_any_transaction_inquiry)
    override fun anyTransactionInquiry() {
        val intent = Intent(this, InquiryInputTransactionActivity::class.java)
        intent.putExtra("pay_title", payTitle)
//        intent.putExtra("pay_channel", payChannel)
        intent.putExtra("isNeedReturnTraceNoOnly", true)
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                val traceNo = data?.getStringExtra("trace_no")
                if (!TextUtils.isEmpty(traceNo)) {
                    nextTitle = StringUtils.getText(configModel.data?.stringFile?.qrInquiryLabel?.label)
                    nextSubTitle = StringUtils.getText(configModel.data?.stringFile?.anyTransactionLabel?.label)
                    presenter.inquiryTransaction(traceNo!!)
                } else {
                    onInquiryTransactionFailure(StringUtils.getText(configModel.data?.stringFile?.transactionNotFoundLabel?.label))
                }
            }
        }
    }

    ///////////////// Report //////////////////
    @OnClick(R.id.bt_summary_report)
    override fun summaryReport() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.summaryReportLabel?.label)
        btSummaryReport.visibility = View.GONE
        btAuditReport.visibility = View.GONE
        btAllPaymentReport.visibility = View.VISIBLE
        btSelectPaymentReport.visibility = View.VISIBLE
    }

    @OnClick(R.id.bt_audit_report)
    override fun auditReport() {
        tvHeaderTitle.text = StringUtils.getText(configModel.data?.stringFile?.auditReportLabel?.label)
        btSummaryReport.visibility = View.GONE
        btAuditReport.visibility = View.GONE
        btAllPaymentReportAudit.visibility = View.VISIBLE
        btSelectReportAudit.visibility = View.VISIBLE
    }

    @OnClick(R.id.bt_all_payment_report)
    override fun allPaymentTypeReport() {
        val intent = Intent(this, ReportSummaryActivity::class.java)
        intent.putExtra("pay_title", tvHeaderTitle.text)
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.bt_select_payment_report)
    override fun selectPaymentTypeReport() {
        val intent = Intent(this, PaymentSelectorActivity::class.java)
        intent.putExtra("show_input_intent", false)
        intent.putExtra("pay_title", tvHeaderTitle.text)
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.REPORT)
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.bt_all_payment_report_audit)
    override fun allPaymentTypeAudit() {
        val intent = Intent(this, ReportAuditActivity::class.java)
        intent.putExtra("pay_title", tvHeaderTitle.text)
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.bt_select_payment_report_audit)
    override fun selectPaymentTypeAudit() {
        val intent = Intent(this, PaymentSelectorActivity::class.java)
        intent.putExtra("show_input_intent", false)
        intent.putExtra("pay_title", payTitle)
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.AUDIT_REPORT)
        startActivity(intent)
        finish()
    }

    @OnClick(R.id.bt_all_payment_settlement)
    override fun allPaymentSettlement() {
    }

    @OnClick(R.id.bt_select_payment_settlement)
    override fun selectPaymentTypeSettlement() {
        val intent = Intent(this, PaymentSelectorActivity::class.java)
        intent.putExtra("show_input_intent", false)
        intent.putExtra("pay_title", payTitle)
        intent.putExtra(PaymentAction.KEY_ACTION, PaymentAction.SETTLEMENT)
        startActivity(intent)
        finish()
    }

    override fun onInquiryTransactionSuccess(transData: TransDataModel, isRePrint: Boolean) {
        if (presenter.isProcessTransactionDone()) return
        val bundle = Bundle()
        val intent = Intent(this, PrintActivity::class.java)
        bundle.putParcelable("transData", transData)
        intent.putExtra("re_print", isRePrint)
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    override fun onInquiryTransactionFailure(message: String) {
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onInquiryTransactionFailure(transData: TransDataModel) {
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionFailLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onInquiryTransactionTimeout(transData: TransDataModel) {
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionTimeoutLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onInquiryTransactionEmpty() {
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.noTransactionLabel?.label), Toast.LENGTH_LONG).show()
    }

    override fun gotoPrintLastTran(
        transData: TransDataModel,
        isRePrint: Boolean,
        payTitle: String,
        paySubTitle: String
    ) {
        val bundle = Bundle()
        val intent = Intent(this, PrintActivity::class.java)
        intent.putExtra("pay_title", nextTitle ?: payTitle)
        intent.putExtra("pay_sub_title", nextSubTitle)
        bundle.putParcelable("transData", transData)
        intent.putExtra("re_print", isRePrint)
        intent.putExtra("slipCount", 0)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun start(context: Activity?, keyAction: String, title: String) {
            val intent = Intent(context, PaymentActionTypeActivity::class.java)
            intent.putExtra("pay_title", title)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, keyAction: String, title: String) {
            val intent = Intent(context, PaymentActionTypeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("pay_title", title)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )

        }
    }

}