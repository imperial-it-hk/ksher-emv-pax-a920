package com.evp.payment.ksher.printing

import android.graphics.Bitmap
import com.evp.payment.ksher.R
import com.evp.payment.ksher.database.DAOAccess
import com.evp.payment.ksher.database.HistoryData
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.printing.generator.*
import com.evp.payment.ksher.utils.StringUtils.getString
import com.evp.payment.ksher.utils.constant.LocalErrorCode
import com.evp.payment.ksher.utils.transactions.TransactionException
import com.evp.payment.ksher.view.dialog.DialogEvent
import com.evp.payment.ksher.view.dialog.DialogUtils.showConfirmCountDown
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class TransactionPrinting : APrinting {
    lateinit var daoAccess: DAOAccess

    /**
     * Whether the receipt is reprint
     */
    private var isRePrint = false

    constructor() {}
    constructor(isRePrint: Boolean) {
        this.isRePrint = isRePrint
    }

    constructor(isRePrint: Boolean, daoAccess: DAOAccess) {
        this.isRePrint = isRePrint
        this.daoAccess = daoAccess
    }

    /**
     * Print latest transaction
     */

    fun printLastTrans(): Completable {
        return Single.fromCallable { daoAccess.getLastTransaction() }
            .flatMapCompletable { printTrans(it, 0) }
            .subscribeOn(Schedulers.io())
    }

    /**
     * Print transaction receipt
     */
    fun printAnyTrans(transData: TransDataModel?): Completable {
        return if (transData == null) {
            Completable.error(
                TransactionException(
                    LocalErrorCode.ERR_NO_TRANS,
                    getString(R.string.transaction_not_found)
                )
            )
        } else printTrans(transData, 0)
    }

    /**
     * Print Settlement receipt
     */
    fun printAnySettlement(
        settlementDataModel: SettlementDataModel?,
    ): Completable {
        return if (settlementDataModel == null) {
            Completable.error(
                TransactionException(
                    LocalErrorCode.ERR_NO_TRANS,
                    getString(R.string.transaction_not_found)
                )
            )
        } else printSettlement(settlementDataModel, 0)
    }


    fun printAnyTrans(transData: TransDataModel?, slipCount: Int): Completable {
        return transData?.let { printTrans(it, slipCount) }
            ?: Completable.error(
                TransactionException(
                    LocalErrorCode.ERR_NO_TRANS,
                    getString(R.string.transaction_not_found)
                )
            )
    }

    private fun printTrans(transData: TransDataModel, currentSlipNum: Int): Completable {
        return Single.fromCallable { TransReceiptGenerator(transData, currentSlipNum, isRePrint) }
            .flatMapCompletable { generator: TransReceiptGenerator -> printBitmap(generator.generate()) }
            .andThen(Single.just(currentSlipNum).flatMapCompletable { i: Int ->
                if (i + 1 >= slipCount) {
                    return@flatMapCompletable Completable.complete()
                }
                showConfirmCountDown(getString(R.string.continue_print_next_sheet_), 5)
                    .flatMapCompletable { event: Int ->
                        if (event == DialogEvent.CANCEL) {
                            return@flatMapCompletable Completable.complete()
                        }
                        printTrans(transData, currentSlipNum + 1)
                    }
            }).onErrorComplete()
            .subscribeOn(Schedulers.io())
    }

    private fun printSettlement(
        settlementDataModel: SettlementDataModel,
        currentSlipNum: Int,
    ): Completable {
        return Single.fromCallable {
            SettlementReceiptGenerator(
                settlementDataModel,
                currentSlipNum,
                isRePrint
            )
        }
            .flatMapCompletable { generator: SettlementReceiptGenerator -> printBitmap(generator.generate()) }
            .andThen(Single.just(currentSlipNum).flatMapCompletable { i: Int ->
                if (i + 1 >= slipCount) {
                    return@flatMapCompletable Completable.complete()
                }
                showConfirmCountDown(getString(R.string.continue_print_next_sheet_), 5)
                    .flatMapCompletable { event: Int ->
                        if (event == DialogEvent.CANCEL) {
                            return@flatMapCompletable Completable.complete()
                        }
                        printSettlement(settlementDataModel, currentSlipNum + 1)
                    }
            })
            .subscribeOn(Schedulers.io())
    }
    /**
     * Print Summary repory receipt
     */
    fun printAnySummaryReport(settlementDataModel: SettlementDataModel?): Completable {
        return if (settlementDataModel == null) {
            Completable.error(
                TransactionException(
                    LocalErrorCode.ERR_NO_TRANS,
                    getString(R.string.transaction_not_found)
                )
            )
        } else printSummaryReport(settlementDataModel, 0)
    }

    private fun printSummaryReport(
        settlementDataModel: SettlementDataModel,
        currentSlipNum: Int
    ): Completable {
        return Single.fromCallable {
            SummaryReceiptGenerator(
                settlementDataModel,
                currentSlipNum,
                isRePrint
            )
        }
            .flatMapCompletable { generator: SummaryReceiptGenerator -> printBitmap(generator.generate()) }
            .andThen(Single.just(currentSlipNum).flatMapCompletable { i: Int ->
                if (i + 1 >= slipCount) {
                    return@flatMapCompletable Completable.complete()
                }
                showConfirmCountDown(getString(R.string.continue_print_next_sheet_), 5)
                    .flatMapCompletable { event: Int ->
                        if (event == DialogEvent.CANCEL) {
                            return@flatMapCompletable Completable.complete()
                        }
                        printSettlement(settlementDataModel, currentSlipNum + 1)
                    }
            })
            .subscribeOn(Schedulers.io())
    }

    /**
     * Print Audit report receipt
     */
    fun printAnyAuditReport(transData: List<HistoryData>?): Completable {
        return if (transData == null) {
            Completable.error(
                TransactionException(
                    LocalErrorCode.ERR_NO_TRANS,
                    getString(R.string.transaction_not_found)
                )
            )
        } else printAuditReport(transData, 0)
    }

    private fun printAuditReport(
        transData: List<HistoryData>,
        currentSlipNum: Int
    ): Completable {
        return Single.fromCallable { AuditReceiptGenerator(transData, currentSlipNum, isRePrint) }
            .flatMapCompletable { generator: AuditReceiptGenerator -> printBitmap(generator.generate()) }
            .andThen(Single.just(currentSlipNum).flatMapCompletable { i: Int ->
                if (i + 1 >= slipCount) {
                    return@flatMapCompletable Completable.complete()
                }
                showConfirmCountDown(getString(R.string.continue_print_next_sheet_), 5)
                    .flatMapCompletable { event: Int ->
                        if (event == DialogEvent.CANCEL) {
                            return@flatMapCompletable Completable.complete()
                        }
                        printAuditReport(transData, currentSlipNum + 1)
                    }
            })
            .subscribeOn(Schedulers.io())
    }
//    private fun printTransNew(transData: TransDataModel, currentSlipNum: Int): Result<String>? {
//        val generator = TransReceiptGenerator(transData, currentSlipNum, isRePrint)
//        printBitmap(generator.generate())
//    }

    fun printAnyTrans(bitmap: Bitmap?): Completable {
        return printBitmap(bitmap)
    }

}