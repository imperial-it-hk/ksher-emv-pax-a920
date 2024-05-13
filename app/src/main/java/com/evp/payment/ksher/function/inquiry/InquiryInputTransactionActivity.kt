package com.evp.payment.ksher.function.inquiry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.inquiry.view.InquiryInputTransactionContact
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.keyboard.KeyboardAmountUtil
import com.evp.payment.ksher.utils.keyboard.KeyboardPasswordUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class InquiryInputTransactionActivity : BaseTimeoutActivity(),
    KeyboardPasswordUtil.KeyboardPasswordListener, InquiryInputTransactionContact.View {

    //    private val payChannel by extraNotNull<String>("pay_channel")
    private val payTitle by extraNotNull<String>("pay_title")
    private val isNeedReturnTraceNoOnly by extra("isNeedReturnTraceNoOnly", false)

    @Inject
    lateinit var transactionRepository: TransactionRepository

    private val presenter: InquiryInputTransactionPresenter by lazy {
        InquiryInputTransactionPresenter(
            this,
            transactionRepository,
            configModel
        )
    }

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivSubHeader: AppCompatImageView

    @BindView(R.id.tv_transaction)
    lateinit var tvAmount: AppCompatTextView

    @BindView(R.id.keyboard_password)
    lateinit var keyboard: ConstraintLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.key_confirm)
    lateinit var btnConfirm: AppCompatButton

    @BindView(R.id.key_cancel)
    lateinit var btnCancel: AppCompatButton

    var keyboardPasswordUtil = KeyboardPasswordUtil()
    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_print_input_transaction

    override fun initViews() {
        keyboardPasswordUtil.init(keyboard, this)
        layoutTitle.visibility = View.VISIBLE
        tvHeaderTitle.visibility = View.VISIBLE
        tvHeaderTitle.text = payTitle
        tvTitle.visibility = View.VISIBLE
        tvTitle.text = StringUtils.getText(configModel.data?.stringFile?.inputTraceLabel?.label)
        ivSubHeader.visibility = View.VISIBLE
        btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        setThemePrimaryColor(btnConfirm)
        setThemePrimaryColor(btnCancel)
//        setupSubHeaderLogo(ivSubHeader, payChannel)
    }

    override fun updateAmount(displayAmount: String?) {
        tvAmount.text = displayAmount
    }

    override fun confirm() {
        if (isNeedReturnTraceNoOnly == false) {
            presenter.inquiryAnyTransaction(tvAmount.text.toString())
        } else {
            setResult(RESULT_OK, Intent().apply {
                putExtra("trace_no", tvAmount.text.toString())
            })
            finish()
        }
    }

    override fun cancel() {
        finish()
//        Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
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

    override fun onTransactionSuccess(transData: TransDataModel) {
        if (presenter.isProcessTransactionDone()) return
        val bundle = Bundle()
        bundle.putParcelable("transData", transData)
        startActivity(PrintActivity::class.java, bundle)
        finish()
    }

    override fun onTransactionFailure(transData: TransDataModel) {
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionFailLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onTransactionTimeout(transData: TransDataModel) {
        if (presenter.isProcessTransactionDone()) return
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionTimeoutLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onTransactionEmpty() {
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.noTransactionLabel?.label), Toast.LENGTH_LONG).show()
    }

    override fun showDialogMessage(title: String) {
        showDialog(
            msg = title,
            actionConfirm = { startCountDown() },
            actionCancel = { startCountDown() })
    }

}