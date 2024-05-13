package com.evp.payment.ksher.utils.device

import android.content.Context
import android.os.BatteryManager
import com.evp.payment.ksher.R
import com.evp.payment.ksher.function.BaseApplication.Companion.appContext
import com.evp.payment.ksher.utils.StringUtils.getString
import com.evp.payment.ksher.utils.ToastUtils.showMessage
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

object BatteryLevelPolling {
    private const val LIMIT_LEVEL = 5
    private const val INTERVAL = 60
    private var disposable: Disposable? = null
    fun polling() {
        if (disposable != null) return
        val context: Context? = appContext
        val batteryManager = context!!.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        Observable.interval(INTERVAL.toLong(), TimeUnit.SECONDS).flatMapCompletable { v: Long? ->
                val batteryLevel =
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (batteryLevel <= LIMIT_LEVEL) {
                    return@flatMapCompletable Completable.complete()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete { showMessage(getString(R.string.low_battery_please_charge)) }
                } else {
                    return@flatMapCompletable Completable.complete()
                }
            }.doOnSubscribe { d: Disposable? -> disposable = d }.onErrorComplete().subscribe()
    }
}