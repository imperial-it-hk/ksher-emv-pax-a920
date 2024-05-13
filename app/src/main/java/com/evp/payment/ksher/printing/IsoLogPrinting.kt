package com.evp.payment.ksher.printing

import android.os.ConditionVariable
import com.evp.payment.ksher.parameter.PrintParam.printIsoLogEnabled
import com.evp.payment.ksher.printing.generator.IsoLogGenerator
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

/**
 * ISO message log print
 */
class IsoLogPrinting : APrinting() {
    private val cv: ConditionVariable = ConditionVariable()
    fun printSend(isoMap: Map<String, ByteArray>?, sendData: ByteArray?) {
        if (!printIsoLogEnabled.get()!!) return
        Single.fromCallable(Callable<IsoLogGenerator> { IsoLogGenerator(isoMap, sendData) })
            .flatMapCompletable { generator: IsoLogGenerator -> printBitmap(generator.generate()) }
            .onErrorComplete().doFinally { cv.open() }.subscribeOn(Schedulers.io()).subscribe()
        cv.block()
    }

    fun printReceive(receiveData: ByteArray?, isoMap: Map<String, ByteArray>?) {
        if (!printIsoLogEnabled.get()!!) return
        Single.fromCallable(Callable<IsoLogGenerator> { IsoLogGenerator(receiveData, isoMap) })
            .flatMapCompletable { generator: IsoLogGenerator -> printBitmap(generator.generate()) }
            .onErrorComplete().doFinally { cv.open() }.subscribeOn(Schedulers.io()).subscribe()
        cv.block()
    }

    fun printList(list: List<String>?) {
        if (!printIsoLogEnabled.get()!!) return
        Single.fromCallable { IsoLogGenerator(list) }
            .flatMapCompletable { generator: IsoLogGenerator -> printBitmap(generator.generate()) }
            .onErrorComplete().doFinally { cv.open() }.subscribeOn(Schedulers.io()).subscribe()
        cv.block()
    }

    init {
        showPrompt = false
    }
}