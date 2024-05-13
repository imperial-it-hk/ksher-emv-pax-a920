package com.evp.payment.ksher.function.main

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.extension.extraNotNull
import com.evp.payment.ksher.parameter.PasswordParam
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DeviceUtil
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.constant.PasswordType
import com.evp.payment.ksher.utils.keyboard.KeyboardPasswordUtil
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
import java.util.concurrent.TimeUnit

open class InputPasswordDialog(context: Context, private val builder: Builder) :
    AppCompatDialog(context), KeyboardPasswordUtil.KeyboardPasswordListener {

    @BindView(R.id.layout_header)
    lateinit var layoutHeader: LinearLayout

    @BindView(R.id.tv_amount)
    lateinit var tvAmount: TextView

    @BindView(R.id.keyboard_password)
    lateinit var keyboard: ConstraintLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvTitle: AppCompatTextView

    @BindView(R.id.iv_sub_header_channel)
    lateinit var tvTitleImage: AppCompatImageView

    var keyboardPasswordUtil = KeyboardPasswordUtil()

    @JvmField
    @BindView(R.id.layout_timer)
    var layoutTimer: LinearLayout? = null

    @JvmField
    @BindView(R.id.tv_timer)
    var tvTimer: TextView? = null

    @BindView(R.id.key_confirm)
    lateinit var btnConfirm: AppCompatButton

    @BindView(R.id.key_cancel)
    lateinit var btnCancel: AppCompatButton

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
    private lateinit var configModel:ConfigModel

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
        setContentView(R.layout.activity_input_password)
        val json = SharedPreferencesUtil.getString("config_file", "")
        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
        ButterKnife.bind(this)
        setCancelable(false)
        initViews()
        DeviceUtil.setDeviceStatus()

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
        keyboardPasswordUtil.init(keyboard, this)
        layoutTitle.visibility = View.VISIBLE
        tvTitle.visibility = View.VISIBLE
        tvTitle.text = StringUtils.getText(configModel.data?.stringFile?.inputPasswordLabel?.label)
        btnConfirm.text = StringUtils.getText(configModel.data?.button?.buttonOk?.label)
        btnCancel.text = StringUtils.getText(configModel.data?.button?.buttonCancel?.label)

        layoutHeader.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvAmount.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        btnConfirm.background = shape
        btnCancel.background = shape

    }

    override fun updateAmount(displayAmount: String?) {
        tvAmount.text = displayAmount
    }

    override fun confirm() {
        if(!tvAmount.text.isNullOrEmpty()) {
            when {
                builder.typePassword.equals(PasswordType.MERCHANT, true) -> {
                    if(tvAmount.text.toString() == PasswordParam.merchant.get()){
                        builder.listener?.onSuccess(this) ?: dismiss()
                    }else{
                        displayIncorrectPassword()
                    }
                }
                builder.typePassword.equals(PasswordType.VOID, true) -> {
                    if(tvAmount.text.toString() == PasswordParam.void.get()){
                        builder.listener?.onSuccess(this) ?: dismiss()
                    }else{
                        displayIncorrectPassword()
                    }
                }
                builder.typePassword.equals(PasswordType.SETTLEMENT, true) -> {
                    if(tvAmount.text.toString() == PasswordParam.settlement.get()){
                        builder.listener?.onSuccess(this) ?: dismiss()
                    }else{
                        displayIncorrectPassword()
                    }
                }
                builder.typePassword.equals(PasswordType.ADMIN, true) -> {
                    if(tvAmount.text.toString() == PasswordParam.admin.get()){
                        builder.listener?.onSuccess(this) ?: dismiss()
                    }else{
                        displayIncorrectPassword()
                    }
                }
            }
        }else{
            displayIncorrectPassword()
        }

    }

    private fun displayIncorrectPassword() {
        keyboardPasswordUtil.clear()
        tvAmount.text = ""
        Toast.makeText(context, StringUtils.getText(configModel.data?.stringFile?.passwordIsIncorrectLabel?.label), Toast.LENGTH_LONG
        ).show()
    }

    override fun cancel() {
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

    interface OnPasswordActionListener {
        fun onSuccess(dialogFragment: InputPasswordDialog)
        fun onFail(dialogFragment: InputPasswordDialog)
    }

    data class Builder(val context: Context,val typePassword: String, var listener: OnPasswordActionListener? = null) {
        fun build() = InputPasswordDialog(context, this)
        fun show() {
            build().show()
        }
    }

}