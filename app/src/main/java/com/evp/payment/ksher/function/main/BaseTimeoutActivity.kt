package com.evp.payment.ksher.function.main

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import butterknife.BindView
import com.evp.payment.ksher.R
import com.evp.payment.ksher.extension.gone
import com.evp.payment.ksher.extension.visible
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.StringUtils
import com.evp.payment.ksher.utils.messenger.TimeoutEvent
import com.evp.payment.ksher.utils.messenger.CancelEvent
import com.evp.payment.ksher.utils.messenger.RxBus
import com.evp.payment.ksher.utils.messenger.TransactionActionEvent
import com.evp.payment.ksher.view.progressbar.ProgressNotifier
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
abstract class BaseTimeoutActivity : BaseActivity() {
    @JvmField
    @BindView(R.id.layout_timer)
    var layoutTimer: LinearLayout? = null

    @JvmField
    @BindView(R.id.tv_timer)
    var tvTimer: TextView? = null

    @Nullable
    @BindView(R.id.rootView)
    lateinit var rootView: ConstraintLayout

    @Nullable
    @BindView(R.id.layout_header)
    lateinit var layoutHeader: LinearLayout
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setThemePrimaryColor(layoutHeader)
        setThemeSecondaryColor(rootView)
    }

    override fun onResume() {
        super.onResume()
        if (autoStartTickTimer) startCountDown()
    }

    override fun onPause() {
        super.onPause()
        stopCountDown()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (clickResetTickTimerEnabled) startCountDown()
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Start countdown, default is 60s
     */
    protected fun startCountDown() {
        if (countDownDisposable != null && !countDownDisposable!!.isDisposed) {
            countDownDisposable!!.dispose()
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
    protected fun stopCountDown() {
        if (countDownDisposable != null && !countDownDisposable!!.isDisposed) {
            countDownDisposable!!.dispose()
        }
    }

    /**
     * Disable countdown and tap the screen without resetting
     */
    fun disableCountDown() {
        // 禁用自启动倒计时
        // Disable auto-start countdown
        autoStartTickTimer = false
        // 禁用点击倒计时
        // Disable click to reset countdown
        clickResetTickTimerEnabled = false
        stopCountDown()
    }

    /**
     * Start countdown
     */
    fun enableCountDown() {
        autoStartTickTimer = true
        clickResetTickTimerEnabled = true
        startCountDown()
    }

    /**
     * Disable and hide countdown
     */
    fun disableAndHideCountDown() {
        // 禁用自启动倒计时
        // Disable auto-start countdown
        autoStartTickTimer = false
        // 禁用点击倒计时
        // Disable click to reset countdown
        clickResetTickTimerEnabled = false
        // 隐藏倒计时
        // Hide countdown
        if (layoutTimer != null) {
            layoutTimer!!.visibility = View.GONE
        }
        stopCountDown()
    }

    override fun onBackPressed() {
//        if (OnClickUtils.isShakeClick(this)) return;
        sendCancelEvent()
    }

    protected open fun onTimeout() {
        ProgressNotifier.getInstance().dismiss()
        sendTimeoutEvent()
    }

    protected fun sendTimeoutEvent() {
        RxBus.getDefault().send(TimeoutEvent())
        stopCountDown()
        finish()
    }

    protected fun sendCancelEvent() {
        RxBus.getDefault().send(CancelEvent())
        stopCountDown()
        finish()
    }

    protected fun sendConfirmEvent(event: TransactionActionEvent) {
        RxBus.getDefault().send(event)
        stopCountDown()
    }

    protected fun setupSubHeaderLogo(ivTitleImage: ImageView, payChannel: String) {
        for (payment in configModel.data?.menusMain?.get(0)?.payment.orEmpty()){
            if(payChannel.equals(payment?.paymentType, true)){
                ivTitleImage.visible()
                ivTitleImage.setImageBitmap(StringUtils.getImage(payment?.icon))
            }
        }

//        ivTitleImage.visible()
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