package com.evp.payment.ksher.function.config

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.evp.payment.ksher.R
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.invisible
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


class CommunicationTypeDialog(context: Context, private val builder: Builder) :
    AppCompatDialog(context) {

    @BindView(R.id.header_title)
    lateinit var tvHeaderTitle: AppCompatTextView

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

    @BindView(R.id.lay_communication)
    lateinit var layoutMenu: LinearLayout

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

    private lateinit var configModel: ConfigModel

    @OnClick(R.id.layout_back)
    fun onBackClick() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_communication_type)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        ButterKnife.bind(this)
        setCancelable(false)
        configModel = Gson().fromJson(SharedPreferencesUtil.getString("config_file", "").get().toString(), ConfigModel::class.java)
        initViews()

        if (autoStartTickTimer) startCountDown()

        val params = window?.attributes
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        params?.height = ViewGroup.LayoutParams.MATCH_PARENT
        window?.attributes = params as WindowManager.LayoutParams
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (clickResetTickTimerEnabled) startCountDown()
        return super.dispatchTouchEvent(ev)
    }

    fun initViews() {
        layoutHeader.setBackgroundColor(Color.parseColor(configModel.data?.setting?.colorPrimary))

        tvHeaderTitle.text = builder.title
        val commuDefault: String = if(builder.currentCommu.isNotEmpty()){
            builder.currentCommu
        }else{
            "MOBILE"
        }
        layoutHeader.visible()
        try {
            val arrayCommu = mutableListOf("MOBILE", "WIFI", "DEMO")
            for (numb in arrayCommu) {
                val view = LayoutInflater.from(context).inflate(R.layout.view_timeout, null)
                view.tv_time_amount.text = numb
                view.tv_time_unit.gone()
                if (commuDefault.equals(numb, true)) {
                    view.iv_mobile.visible()
                } else {
                    view.iv_mobile.invisible()
                }
                view.lay_timeout_row.setOnClickListener {
                    stopCountDown()
                    builder.listener?.onSuccess(numb, this) ?: dismiss()
                }
                layoutMenu.addView(view)

            }
        }catch (e: Exception){
            Toast.makeText(context, StringUtils.getText(configModel.data?.stringFile?.unknownErrorLabel?.label),Toast.LENGTH_SHORT).show()
        }
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
        fun onSuccess(language: String, dialogFragment: CommunicationTypeDialog)
        fun onFail(dialogFragment: CommunicationTypeDialog)
    }

    data class Builder(
        val context: Context, val title: String, val currentCommu: String,var listener: OnActionListener? = null) {
        fun build() = CommunicationTypeDialog(context, this)
        fun show() {
            build().show()
        }
    }

}