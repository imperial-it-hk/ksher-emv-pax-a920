package com.evp.payment.ksher.function.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.OnClick
import com.evp.payment.ksher.BuildConfig
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.repository.SuspendedRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.database.repository.TransactionRepository
import com.evp.payment.ksher.extension.extra
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.function.main.BaseTimeoutActivity
import com.evp.payment.ksher.function.payment.action.print.PrintActivity
import com.evp.payment.ksher.function.payment.view.PaymentAmountConfirmContact
import com.evp.payment.ksher.function.qr.ScanCodeActivity
import com.evp.payment.ksher.function.qr.ScanCodeRequestEvent
import com.evp.payment.ksher.invoke.InvokeResponseDescriptionData
import com.evp.payment.ksher.invoke.InvokeResponseHeaderData
import com.evp.payment.ksher.invoke.InvokeResponseModel
import com.evp.payment.ksher.parameter.ControllerParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.printing.TransactionPrinting
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.InvokeConstant
import com.evp.payment.ksher.utils.constant.PaymentAction
import com.evp.payment.ksher.view.dialog.DialogUtils
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import com.google.gson.Gson
import io.reactivex.Completable
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class PaymentAmountConfirmActivity : BaseTimeoutActivity(), PaymentAmountConfirmContact.View {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var suspendedRepository: SuspendedRepository

    val presenter: PaymentAmountConfirmContact.Presenter by lazy {
        PaymentAmountConfirmPresenter(
            this,
            transactionRepository,
            suspendedRepository,
            configModel
        )
    }

    @BindView(R.id.layout_menu)
    lateinit var layoutMenu: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutSubHeader: LinearLayout

    @BindView(R.id.btn_cancel)
    lateinit var btnCancel: Button

    @BindView(R.id.btn_confirm)
    lateinit var btnConfirm: Button

    @BindView(R.id.tv_detail_amount)
    lateinit var txtAmount: TextView

    @BindView(R.id.tv_detail_title)
    lateinit var txtAmountTitle: TextView

    var subscribeEnableCancelButton: Disposable? = null

    private val payChannel by extraNotNull<String>("pay_channel")
    private val payAmount by extraNotNull<String>("pay_amount")
    private val authCode by extraNotNull<String>("auth_code")
    private val mediaType by extra<String>("media_type", "")

    override fun initViews() {
        layoutSubHeader.visibility = View.VISIBLE
        initMenu()
        startEnableCancelButtonCountdown()
        setThemePrimaryColor(btnConfirm)
    }


    override fun loadParam() {
    }

    override val layoutId: Int
        get() = R.layout.activity_payment_amount_confirm


    override fun onResume() {
        super.onResume()
        // Disable status bar, home, recent key
        DeviceUtil.setDeviceStatus()
    }

    override fun onPause() {
        super.onPause()
//        if (subscribeEnableCancelButton?.isDisposed == false) {
//            subscribeEnableCancelButton?.dispose()
//        }
    }

    @OnClick(R.id.layout_back)
    fun onBackClick() {
//        onBackPressed()
    }


    override fun initMenu() {
        txtAmount.text = payAmount
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)
        btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonConfirm?.label)
//        txtAmountTitle.text = StringUtils.getText(configModel.data?.menusMain?.get(0)?.label?)
//        Toast.makeText(this, authCode, Toast.LENGTH_SHORT).show()
    }

    @OnClick(R.id.btn_confirm)
    override fun confirm() {
        presenter.initialTransaction(payChannel, payAmount, authCode)
    }

    @OnClick(R.id.btn_cancel)
    override fun cancel() {
        onBackPressed()
    }

    override fun displayAmount() {
//        Toast.makeText(this, "amount", Toast.LENGTH_SHORT).show()
    }

    override fun onQuickPayError(error: String, transData: TransDataModel) {
        invokeMerchant(transData, InvokeConstant.TRANSACTION_TYPE_SALE, mediaType, "005", error)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }

    override fun onTransactionSuccess(transData: TransDataModel) {
        CoroutineScope(Dispatchers.IO).launch {
            transactionRepository.insertTransaction(transData).collect {
                val transResult = it?.getOrNull()
                SystemParam.incInvoiceNo()
                ControllerParam.needPrintLastTrans.set(true)

                Completable.fromAction(ProgressNotifier.getInstance()::show)
                    .andThen(TransactionPrinting().printAnyTrans(transResult))
                    .doOnComplete {
                        ControllerParam.needPrintLastTrans.set(false)

                        if (!mediaType.isNullOrEmpty()) {
                            PrintActivity.startWithInvoke(
                                this@PaymentAmountConfirmActivity,
                                mediaType,
                                it?.getOrNull()
                            )
                        } else {
                            PrintActivity.start(this@PaymentAmountConfirmActivity, it?.getOrNull())
                        }
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

    override fun onTransactionFailure(transData: TransDataModel) {
        invokeMerchant(transData, InvokeConstant.TRANSACTION_TYPE_SALE, mediaType, "0005", "Transaction Failure.")
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionFailLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onTransactionTimeout(transData: TransDataModel) {
        invokeMerchant(transData, InvokeConstant.TRANSACTION_TYPE_SALE, mediaType, "0005", "Transaction Timeout.")
        Toast.makeText(this, StringUtils.getText(configModel.data?.stringFile?.transactionTimeoutLabel?.label), Toast.LENGTH_LONG).show()
        finish()
    }

    override fun showDialogMessage(transData: TransDataModel, msg: String) {
        showDialog(msg = msg, actionCancel = {
            invokeMerchant(
                transData,
                InvokeConstant.TRANSACTION_TYPE_SALE,
                mediaType,
                "0005",
                "Transaction NOTSURE."
            )
            finish()
        }, actionConfirm = {
            invokeMerchant(
                transData,
                InvokeConstant.TRANSACTION_TYPE_SALE,
                mediaType,
                "0005",
                "Transaction NOTSURE."
            )
            finish()
        })
    }

    private fun startEnableCancelButtonCountdown() {
        val startTimeMillis = System.currentTimeMillis();
        var countDownSecond: Long = 11
        subscribeEnableCancelButton = Observable.timer(1000, TimeUnit.MILLISECONDS)
            .take(11)
            .repeatUntil {
                System.currentTimeMillis() - startTimeMillis > (11 * 1000)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                btnCancel.isEnabled = true
                btnCancel.text = getString(R.string.cancel)
            }
            .subscribe {
                Timber.d("enable cancel button after : ${System.currentTimeMillis() - startTimeMillis}")
                countDownSecond--
                btnCancel.text = "${getString(R.string.cancel)} ($countDownSecond)"
            }
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
        private const val TAG = "ScanCodeActivity"
        const val BACK = "BACK"
        const val TIMEOUT = "TIMEOUT"
        const val MANUAL_INPUT = "MANUAL_INPUT"
        const val CODE = "CODE"

        @JvmStatic
        fun start(context: Activity?, result: String, payAmount: String?, payChannel: String) {
            val intent = Intent(context, PaymentAmountConfirmActivity::class.java)
            intent.putExtra("auth_code", result)
            intent.putExtra("pay_amount", payAmount)
            intent.putExtra("pay_channel", payChannel)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

        @JvmStatic
        fun startWithInvoke(
            context: Activity?,
            result: String,
            payAmount: String?,
            payChannel: String,
            mediaType: String?
        ) {
            val intent = Intent(context, PaymentAmountConfirmActivity::class.java)
            intent.putExtra("auth_code", result)
            intent.putExtra("pay_amount", payAmount)
            intent.putExtra("pay_channel", payChannel)
            intent.putExtra("media_type", mediaType)
            context?.startActivity(intent)
            context?.overridePendingTransition(
                R.anim.animation_slide_right_in, R.anim.animation_slide_right_out
            )
            context?.finish()
        }

    }

}