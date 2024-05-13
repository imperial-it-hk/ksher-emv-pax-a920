package com.evp.payment.ksher.view.dialog

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

object DialogUtils {
    /**
     * Success, dialog display timeout, unit s
     */
    const val TIMEOUT_SUCCESS = 1

    /**
     * Failed, dialog display timeout, unit s
     */
    const val TIMEOUT_FAIL = 5
    fun showAlert(content: String?): Completable {
        return Single.fromCallable {
            DialogParameter().setContent(content).setCancelEnabled(false)
        }.observeOn(AndroidSchedulers.mainThread())
            .flatMap { param: DialogParameter? -> RxDialog().show(param) }.ignoreElement()
    }

    fun showAlertTimeout(content: String?, timeout: Int): Completable {
        return Single.fromCallable {
            DialogParameter().setContent(content).setCancelEnabled(false).setTimeout(timeout)
        }.observeOn(AndroidSchedulers.mainThread())
            .flatMap { param: DialogParameter? -> RxDialog().show(param) }.ignoreElement()
    }

    fun showConfirm(content: String?): Single<Int> {
        return Single.fromCallable {
            DialogParameter().setContent(content)
        }.observeOn(AndroidSchedulers.mainThread())
            .flatMap { param: DialogParameter? -> RxDialog().show(param) }
    }

    /**
     * Display countdown on confirm button
     */
    fun showConfirmCountDown(content: String?, countDown: Int): Single<Int> {
        return Single.fromCallable {
            DialogParameter().setContent(content).setCountDown(countDown)
        }.observeOn(AndroidSchedulers.mainThread())
            .flatMap { param: DialogParameter? -> RxDialog().show(param) }
    }

    fun showConfirm(content: String?, cancelText: String?, confirmText: String?): Single<Int> {
        return Single.fromCallable {
            DialogParameter().setContent(content).setCancelText(cancelText!!)
                .setConfirmText(confirmText!!)
        }.observeOn(AndroidSchedulers.mainThread())
            .flatMap { param: DialogParameter? -> RxDialog().show(param) }
    }
}