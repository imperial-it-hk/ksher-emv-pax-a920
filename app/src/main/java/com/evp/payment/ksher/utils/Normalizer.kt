package com.evp.payment.ksher.utils

import com.evp.payment.ksher.R
import com.evp.payment.ksher.parameter.AppStoreParam
import com.evp.payment.ksher.printing.generator.TransReceiptGenerator
import com.evp.payment.ksher.view.dialog.DialogEvent
import com.evp.payment.ksher.view.dialog.DialogUtils
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class Normalizer {
    fun normalize(isForceLoadConfig: Boolean): Completable {
        return Single.fromCallable { AppStoreParam.updateParams(isForceLoadConfig) }.doOnSuccess {  }.ignoreElement()
//        return Completable.complete().andThen(AppStoreParam.updateParams())
    }
}