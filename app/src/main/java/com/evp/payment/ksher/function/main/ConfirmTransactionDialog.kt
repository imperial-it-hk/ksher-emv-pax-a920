package com.evp.payment.ksher.function.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.toAmount2DigitDisplay
import com.evp.payment.ksher.extension.toPaymentChannelDisplay
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.messenger.CancelEvent
import com.evp.payment.ksher.utils.messenger.RxBus
import com.evp.payment.ksher.utils.messenger.TimeoutEvent
import com.evp.payment.ksher.utils.messenger.TransactionActionEvent
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_settlement.*
import kotlinx.android.synthetic.main.dialog_confirm_transaction.*
import java.util.concurrent.TimeUnit

open class ConfirmTransactionDialog(context: Context, private val builder: Builder) :
    AppCompatDialog(context) {

    @BindView(R.id.layout_header)
    lateinit var layoutHeader: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_title)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubTitle: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var tvTitleImage: AppCompatImageView

    @JvmField
    @BindView(R.id.tv_timer)
    var tvTimer: TextView? = null

    @BindView(R.id.tv_tranType_title)
    lateinit var tvTranTypeTitle: AppCompatTextView

    @BindView(R.id.tv_tran_type_amount)
    lateinit var tvTranTypeAmount: AppCompatTextView

    @BindView(R.id.tv_wallet_title)
    lateinit var tvWalletTitle: AppCompatTextView

    @BindView(R.id.tv_wallet_amount)
    lateinit var tvWalletAmount: AppCompatTextView

    @BindView(R.id.tv_trace_no_title)
    lateinit var tvTraceNoTitle: AppCompatTextView

    @BindView(R.id.tv_trace_no)
    lateinit var tvTraceNo: AppCompatTextView

    @BindView(R.id.tv_amount_title)
    lateinit var tvAmountTitle: AppCompatTextView

    @BindView(R.id.tv_amount)
    lateinit var tvAmount: AppCompatTextView

    private lateinit var configModel: ConfigModel

    /**
     * Default timeout: 60s
     */
    protected var timeout = SystemParam.transactionTimeout.get()?.toInt()!!

    /**
     * Whether to start the countdown automatically
     */
    protected var autoStartTickTimer = true

    /**
     * Whether to restart countdown when click screen
     */
    protected var clickResetTickTimerEnabled = true

    /**
     * Countdown disposable
     */
    private var countDownDisposable: Disposable? = null

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(R.layout.dialog_confirm_transaction)
        ButterKnife.bind(this)
        setCancelable(false)
        configModel = Gson().fromJson(SharedPreferencesUtil.getString("config_file", "").get().toString(), ConfigModel::class.java)
        initViews()

        if (autoStartTickTimer) startCountDown()

        val params = window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = params as android.view.WindowManager.LayoutParams
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (clickResetTickTimerEnabled) startCountDown()
        return super.dispatchTouchEvent(ev)
    }

    fun initViews() {
        layoutTitle.visible()
        layoutHeader.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        btn_confirm.background = shape
        setTextScreen()

        if (builder.title.isNotEmpty()) {
            tvTitle.visible()
            tvTitle.text = builder.title
        }

        if (!builder.transData.paymentChannel.isNullOrEmpty()) {
            tvTitleImage.visible()
            setupSubHeaderLogo(tvTitleImage, builder.transData.paymentChannel.orEmpty())
        }

        if (builder.subtitle.isNotEmpty()) {
            tvSubTitle.visible()
            tvSubTitle.text = builder.subtitle
        }

        btn_cancel.text = StringUtils.getText(configModel.data?.button?.buttonDelete?.label)
        btn_confirm.text = StringUtils.getText(configModel.data?.button?.buttonConfirm?.label)
        bindingData(builder.transData)
    }

    private fun setTextScreen() {
        tvTranTypeTitle.text = StringUtils.getText(configModel.data?.stringFile?.transTypeLabel?.label)
        tvWalletTitle.text = StringUtils.getText(configModel.data?.stringFile?.walletLabel?.label)
        tvAmountTitle.text = StringUtils.getText(configModel.data?.stringFile?.amountLabel?.label)
        tvTraceNoTitle.text = StringUtils.getText(configModel.data?.stringFile?.traceNoLabel?.label)

        tvTranTypeTitle.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvWalletTitle.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvAmountTitle.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvTraceNoTitle.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))

        tvTranTypeAmount.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvWalletAmount.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvAmount.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvTraceNo.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
    }

    @SuppressLint("SetTextI18n")
    private fun bindingData(transData: TransDataModel) {
        tvTranTypeAmount.text = transData.transType
        tvWalletAmount.text = transData.paymentChannel?.toPaymentChannelDisplay()
        tvAmount.text = "THB ${transData.amount?.toAmount2DigitDisplay()}"
        tvTraceNo.text = "${String.format("%06d", transData.traceNo)}"
    }

    @OnClick(R.id.btn_confirm)
    fun confirmTransaction() {
        stopCountDown()
        builder.listener?.onSuccess(builder.transData, this) ?: dismiss()
    }

    @OnClick(R.id.btn_cancel)
    fun cancelTransaction() {
        stopCountDown()
        builder.listener?.onFail(this) ?: dismiss()
    }

    /**
     * Start countdown, default is 60s
     */
    private fun startCountDown() {
        if (countDownDisposable != null && !countDownDisposable!!.isDisposed) {
            countDownDisposable?.dispose()
        }
        Observable.intervalRange(0, timeout + 1L, 0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { t: Long ->
                if (tvTimer == null) return@doOnNext
                tvTimer?.text = (timeout - t).toString() + "s"
            }
            .doOnComplete { onTimeout() }
            .doOnSubscribe { d: Disposable? -> countDownDisposable = d }
            .subscribe()
    }

    /**
     * Stop countdown
     */
    private fun stopCountDown() {
        if (countDownDisposable != null && !countDownDisposable!!.isDisposed) {
            countDownDisposable?.dispose()
        }
    }

    override fun onBackPressed() {
        sendCancelEvent()
    }

    protected open fun onTimeout() {
        sendTimeoutEvent()
    }

    protected fun sendTimeoutEvent() {
        RxBus.getDefault().send(TimeoutEvent())
        stopCountDown()
        if (this.ownerActivity != null && this.ownerActivity?.isFinishing == false && this.isShowing)
            dismiss()
    }

    private fun sendCancelEvent() {
        RxBus.getDefault().send(CancelEvent())
        stopCountDown()
        dismiss()
    }

    protected fun sendConfirmEvent(event: TransactionActionEvent) {
        RxBus.getDefault().send(event)
        stopCountDown()
    }

    interface OnActionListener {
        fun onSuccess(transData: TransDataModel, dialogFragment: ConfirmTransactionDialog)
        fun onFail(dialogFragment: ConfirmTransactionDialog)
    }

    data class Builder(
        val context: Context, val title: String, val subtitle: String,
        val transData: TransDataModel, var listener: OnActionListener? = null
    ) {
        fun build() = ConfirmTransactionDialog(context, this)
        fun show() {
            build().show()
        }
    }

    protected fun setupSubHeaderLogo(ivTitleImage: ImageView, payChannel: String) {
        for (payment in configModel.data?.menusMain?.get(0)?.payment.orEmpty()){
            if(payChannel.equals(payment?.paymentType, true)){
                ivTitleImage.visible()
                ivTitleImage.setImageBitmap(StringUtils.getImage(payment?.icon))
            }
        }
//        when {
//            "linepay".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_linepay)
//            }
//            "promptpay".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_promptpay)
//            }
//            "alipay".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_alipay)
//            }
//            "wechat".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_wechat)
//            }
//            "truemoney".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_true_money)
//            }
//            "airpay".equals(payChannel, true) -> {
//                ivTitleImage.setImageResource(R.drawable.ic_payment_shopee)
//            }
//            else -> ivTitleImage.gone()
//        }
    }
}