package com.evp.payment.ksher.function.config

import android.R.attr.maxLength
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
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
import kotlinx.android.synthetic.main.view_timeout.view.*
import java.util.concurrent.TimeUnit


class InputTextDialog(context: Context, private val builder: Builder) :
    AppCompatDialog(context) {

    @BindView(R.id.layout_header)
    lateinit var layoutHeader: LinearLayout

    @BindView(R.id.layout_sub_header)
    lateinit var layoutTitle: LinearLayout

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

    @BindView(R.id.tv_sub_header_detail)
    lateinit var tvSubTitle: AppCompatTextView

    @BindView(R.id.tv_title)
    lateinit var tvTextTitle: AppCompatTextView

    @BindView(R.id.ed_input)
    lateinit var edInput: AppCompatEditText

    @BindView(R.id.btn_ok)
    lateinit var btnOk: AppCompatButton

    @JvmField
    @BindView(R.id.tv_timer)
    var tvTimer: TextView? = null

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
        setContentView(R.layout.dialog_text_input)
        ButterKnife.bind(this)
        setCancelable(false)
        val json = SharedPreferencesUtil.getString("config_file", "")
        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)
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
        layoutHeader.visible()
        tvHeaderTitle.text = builder.title
        tvTextTitle.text = StringUtils.getText(configModel.data?.stringFile?.pleaseInputLabel?.label)

        layoutHeader.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        tvTextTitle.setTextColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.setColor(Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.setStroke(6, Color.parseColor(configModel.data?.setting?.colorPrimary))
        shape.cornerRadii = floatArrayOf(6f, 6f, 6f, 6f, 6f, 6f, 6f, 6f)
        btnOk.background = shape

        if(builder.oldValue.isNotEmpty()){
            edInput.text = Editable.Factory.getInstance().newEditable(builder.oldValue)
        }

        if(builder.useKeyboardLanguage){
            edInput.inputType = InputType.TYPE_CLASS_TEXT
        }

        if(builder.title.orEmpty().equals(StringUtils.getText(configModel.data?.stringFile?.terminalIDLabel?.label), true)){
            edInput.filters = arrayOf<InputFilter>(LengthFilter(8))
        }
    }

    @OnClick(R.id.btn_ok)
    fun updateResult(){
        builder.listener?.onSuccess(edInput.text.toString(), this)
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
        builder.listener?.onFail(this) ?: dismiss()
        dismiss()
    }

    protected fun sendConfirmEvent(event: TransactionActionEvent) {
        RxBus.getDefault().send(event)
        stopCountDown()
    }

    interface OnActionListener {
        fun onSuccess(result: String, dialogFragment: InputTextDialog)
        fun onFail(dialogFragment: InputTextDialog)
    }

    data class Builder(
        val context: Context,val title: String?, val oldValue: String, val useKeyboardLanguage: Boolean, var listener: OnActionListener? = null) {
        fun build() = InputTextDialog(context, this)
        fun show() {
            build().show()
        }
    }

}