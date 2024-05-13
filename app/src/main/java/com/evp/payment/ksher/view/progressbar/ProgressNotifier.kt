package com.evp.payment.ksher.view.progressbar

import android.widget.Toast
import com.evp.payment.ksher.function.BaseApplication
import com.evp.payment.ksher.parameter.SystemParam
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class ProgressNotifier {
    private var progressBarParameter: ProgressBarParameter? = null
    private var progressBarDialog: ProgressBarDialog? = null
        get() {
            if (field == null) {
                field = ProgressBarDialog()
            }
            return field
        }
    private var notifier: BehaviorSubject<ProgressBarParameter?> = BehaviorSubject.create()
    private var timeoutDisposable: Disposable? = null
    fun show() {
        progressBarParameter = ProgressBarParameter()
        if (isNotifyEnabled) {
            notifier.onComplete()
        }
        notifier = BehaviorSubject.create()
        notifier.observeOn(AndroidSchedulers.mainThread())
            .doOnNext { p: ProgressBarParameter? ->
                p?.let {
                    progressBarDialog?.show(
                        it
                    )
                }

            }
            .doOnComplete {
                progressBarDialog?.dismiss()
            }
            .subscribe()

        Observable.intervalRange(0, SystemParam.connectionTimeout.get()?.toLong()!!-2L, 0, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete { dismissTimeout() }
            .doOnSubscribe { d: Disposable? -> timeoutDisposable = d }
            .subscribe()
    }

    fun dismissTimeout() {
        if (timeoutDisposable != null && !timeoutDisposable!!.isDisposed) {
            timeoutDisposable?.dispose()
        }

        Toast.makeText(BaseApplication.appContext, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
        if (!isNotifyEnabled) return
        notifier.onComplete()
    }

    fun dismiss() {
        if (timeoutDisposable != null && !timeoutDisposable!!.isDisposed) {
            timeoutDisposable?.dispose()
        }
        if (!isNotifyEnabled) return
        notifier.onComplete()
    }

    fun hideProgress(isHide: Boolean) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = false
            it.isHideProgress = isHide
            notifier.onNext(it)
        }
    }

    fun primaryContentOnly(content: String?) {
        if (!isNotifyEnabled) return

        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = false
            it.primaryContent = content
            it.subContent = ""
            it.timeout = -1
            notifier.onNext(it)
        }
    }

    fun primaryContent(content: String?) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = false
            it.primaryContent = content
            notifier.onNext(it)
        }
    }

    fun subContent(content: String?) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = false
            it.subContent = content
            notifier.onNext(it)
        }
    }

    fun showApprove(content: String?) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isDeclined = false
            it.isApproved = true
            it.primaryContent = content
            it.timeout = -1
            notifier.onNext(it)
        }
    }

    fun showDecline(content: String?) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = true
            it.primaryContent = content
            it.timeout = -1
            notifier.onNext(it)
        }
    }

    fun timeout(timeout: Long) {
        if (!isNotifyEnabled) return
        progressBarParameter?.let {
            it.isApproved = false
            it.isDeclined = false
            it.timeout = timeout
            notifier.onNext(it)
        }
    }

    fun timeoutDismiss(timeout: Int) {
        if (!isNotifyEnabled) return
        if (timeoutDisposable != null && !timeoutDisposable!!.isDisposed) {
            timeoutDisposable!!.dispose()
        }
        Observable.intervalRange(0, timeout + 1L, 0, 1, TimeUnit.SECONDS)
            .doOnNext { t: Long ->
                progressBarParameter?.let {
                    it.timeout = timeout - t
                    notifier.onNext(it)
                }
            }
            .doOnTerminate { dismiss() }
            .doOnSubscribe { d: Disposable? -> timeoutDisposable = d }
            .subscribe()
    }

    private val isNotifyEnabled: Boolean
        private get() = notifier != null && !notifier.hasComplete()

    companion object {
        @JvmStatic
        private var instance: ProgressNotifier? = null

        @JvmStatic
        fun getInstance(): ProgressNotifier {
            if (instance == null) instance = ProgressNotifier()
            return instance!!
        }
    }
}