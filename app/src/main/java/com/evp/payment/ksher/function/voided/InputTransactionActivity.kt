package com.evp.payment.ksher.function.voided

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.main.ConfirmTransactionDialog
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.qr.ScanCodeActivity
import com.evp.payment.ksher.function.qr.ScanCodeRequestEvent
import com.evp.payment.ksher.function.voided.view.InputTransactionContact
import com.evp.payment.ksher.invoke.InvokeResponseDescriptionData
import com.evp.payment.ksher.invoke.InvokeResponseHeaderData
import com.evp.payment.ksher.invoke.InvokeResponseModel
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.utils.keyboard.KeyboardPasswordUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class InputTransactionActivity : BaseTimeoutActivity(),
    KeyboardPasswordUtil.KeyboardPasswordListener, InputTransactionContact.View {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    val presenter: InputTransactionPresenter by lazy {
        InputTransactionPresenter(
            this,
            transactionRepository,
            configModel
        )
    }

    @BindView(R.id.tv_transaction)
    lateinit var tvAmount: AppCompatTextView

    @BindView(R.id.keyboard_password)
    lateinit var keyboard: ConstraintLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var ivTitleImage: AppCompatImageView

    @BindView(R.id.key_confirm)
    lateinit var btnConfirm: AppCompatButton

    @BindView(R.id.key_cancel)
    lateinit var btnCancel: AppCompatButton

    private val ACTION_METHOD_TYPE by extraNotNull<String>(PaymentAction.KEY_ACTION)
//    private val payChannel by extraNotNull<String>("pay_channel")

    var keyboardPasswordUtil = KeyboardPasswordUtil()
    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_input_transaction

    override fun initViews() {
        keyboardPasswordUtil.init(keyboard, this)
        layoutTitle.visibility = View.VISIBLE
        tvTitle.visibility = View.VISIBLE
        tvTitle.text = StringUtils.getText(configModel.data?.stringFile?.inputTraceNumberLabel?.label)
        btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)

        setThemePrimaryColor(btnConfirm)
        setThemePrimaryColor(btnCancel)
//        setupSubHeaderLogo(ivTitleImage, payChannel)
    }


    override fun updateAmount(displayAmount: String?) {
        tvAmount.text = displayAmount
    }

    override fun confirm() {
        presenter.initialTransaction(tvAmount.text.toString())
    }

    override fun cancel() {
        onBackClick()
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

    val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                tvAmount.text = result.data?.data.toString()
            }
        }

    @OnClick(R.id.bt_scan)
    override fun scanQR() {
        val bundle = Bundle()
        val requestEvent = ScanCodeRequestEvent()
        requestEvent.title = tvTitle.text.toString()
        requestEvent.isNotifyByEvent = true
        requestEvent.isManualInputEnabled = true
        bundle.putSerializable(ScanCodeRequestEvent::class.java.simpleName, requestEvent)
//        startActivity(ScanCodeActivity::class.java, bundle)

        startForResult.launch(Intent(this, ScanCodeActivity::class.java).apply {
            putExtras(bundle)
            putExtra(PaymentAction.KEY_ACTION, ACTION_METHOD_TYPE)
            putExtra("pay_channel", title)
        })
    }

    override fun onVoidFail(message: String, transData: TransDataModel) {
        invokeMerchant(transData, transData.transType, "00005", message)
        showDialog(
            msg = message,
            actionCancel = { },
            actionConfirm = { }
        )
    }

    override fun gotoPrinting(transData: TransDataModel) {
        ControllerParam.needPrintLastTrans.set(false)
        PrintActivity.start(this, transData)
    }

    override fun reInputOrFail(resourceString: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            ProgressNotifier.getInstance().show()
//            ProgressNotifier.getInstance()
//                .showDecline(BaseApplication.appContext?.getString(resourceString))
//            delay(2000)
//            ProgressNotifier.getInstance().dismiss()
//            backToHome()
//        }
        showDialog(
            msg = resourceString,
            actionCancel = { },
            actionConfirm = { })

    }

    override fun backToHome() {
        finish()
    }

    override fun showDialogConfirm(transData: TransDataModel) {
        val confirmDialog = ConfirmTransactionDialog(
            this, ConfirmTransactionDialog.Builder(this, StringUtils.getText(configModel.data?.stringFile?.voidAndRefundLabel?.label),
                "", transData,  object : ConfirmTransactionDialog.OnActionListener {
                    override fun onSuccess(
                        transData: TransDataModel,
                        dialogFragment: ConfirmTransactionDialog
                    ) {
                        dialogFragment.dismiss()
                        presenter.validateOrigTransData(transData)
                    }

                    override fun onFail(dialogFragment: ConfirmTransactionDialog) {
                        dialogFragment.dismiss()
                    }

                })
        )
        confirmDialog.show()
    }

    private fun invokeMerchant(
        transData: TransDataModel,
        transactionType: String?,
        respCode: String?,
        message: String?
    ) {

        val version: InvokeResponseHeaderData = InvokeResponseHeaderData().apply {
            version = BuildConfig.VERSION_NAME
        }
        val header: InvokeResponseDescriptionData = InvokeResponseDescriptionData().apply {
            this.respcode = respCode
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
                putExtra("transaction_type", transactionType)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            })
        }
    }

    companion object {

        @JvmStatic
        fun start(context: Activity?, keyAction: String) {
            val intent = Intent(context, InputTransactionActivity::class.java)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(context: Activity?, keyAction: String) {
            val intent = Intent(context, InputTransactionActivity::class.java)
            intent.putExtra(PaymentAction.KEY_ACTION, keyAction)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }
    }

}