package com.evp.payment.ksher.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import com.evp.payment.ksher.parameter.SystemParam
import com.evp.payment.ksher.utils.DateUtils

@Dao
interface DAOAccess {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(transDataModel: TransDataModel)

    @Query("SELECT * FROM `Transaction` ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getAllTransaction(limit: Int, offset: Int): List<TransDataModel>?

    @Query("SELECT * FROM `Transaction` WHERE payment_channel =:channel ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getAllTransactionWithPaymentChannel(
        channel: String,
        limit: Int,
        offset: Int
    ): List<TransDataModel>?

    @Query("SELECT * FROM `Transaction` WHERE trace_no =:traceNo AND trans_type =:transactionType")
    fun getTransactionDetail(transactionType: String?, traceNo: String?): TransDataModel


    @Query("SELECT * FROM `Transaction` WHERE orig_trace_no =:oriTraceNo")
    fun getTransactionDetailByOriginalTraceNo(oriTraceNo: String?): TransDataModel

    @Query("SELECT * FROM `Transaction` WHERE trace_no =:traceNo")
    fun getTransactionDetail(traceNo: String?): TransDataModel

    @Query("SELECT * FROM `Transaction` WHERE mch_order_no =:mchOrderNo ORDER BY id DESC LIMIT 1")
    fun getTransactionDetailLatest(mchOrderNo: String?): TransDataModel

    @Query("SELECT MAX(id) FROM `Transaction`")
    fun getLastId(): String

    @Query("SELECT * FROM `Transaction` ORDER BY ID DESC LIMIT 1")
    fun getLastTransaction(): TransDataModel

    @Query("SELECT * FROM `Transaction` WHERE payment_channel=:paymentChannel ORDER BY ID DESC LIMIT 1")
    fun getLastTransactionByChannel(paymentChannel: String?): TransDataModel

    @Query("DELETE FROM `Transaction` WHERE mch_order_no =:mchOrderNo")
    fun deleteTransaction(mchOrderNo: String?)

    @Query("DELETE FROM `Transaction` WHERE id NOT IN (SELECT id FROM `Transaction` ORDER BY id DESC LIMIT :rowToKeep)")
    fun deleteTransactionCircular(rowToKeep: Int)

    @Query("DELETE FROM `SuspendedQr` WHERE id NOT IN (SELECT id FROM `SuspendedQr` ORDER BY id DESC LIMIT :rowToKeep)")
    fun deleteSuspendedQrCircular(rowToKeep: Int)

    @Query("SELECT payment_channel as paymentChannel, SUM(CASE WHEN trans_type = 'SALE' THEN 1 ELSE 0 END) AS saleCount, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'REFUND' THEN 1 ELSE 0 END) AS refundCount, SUM(CASE WHEN trans_type = 'REFUND' THEN amount ELSE 0 END) AS refundTotalAmount FROM `Transaction` WHERE payment_channel=:paymentChannel AND batch_no =:batchNo")
    fun getSettlementSaleAndRefundCountByChannel(
        paymentChannel: String?,
        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
    ): SettlementItemModel

//    @Query("SELECT SUM(CASE WHEN trans_type = 'SALE' THEN 1 ELSE 0 END) AS saleCount, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'REFUND' THEN 1 ELSE 0 END) AS refundCount, SUM(CASE WHEN trans_type = 'REFUND' THEN amount ELSE 0 END) AS refundTotalAmount FROM `Transaction` WHERE batch_no =:batchNo")
//    fun getSettlementSaleAndRefundCountAllChannel(
//        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
//    ): SettlementItemModel


//    @Query("SELECT SUM(CASE WHEN trans_type = 'SALE' THEN 1 ELSE 0 END) AS saleCount, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'REFUND' THEN 1 ELSE 0 END) AS refundCount, SUM(CASE WHEN trans_type = 'REFUND' THEN amount ELSE 0 END) AS refundTotalAmount FROM `Transaction` WHERE payment_channel=:paymentChannel AND batch_no =:batchNo")
//    fun settlementCurrentBatchNo(batchNo: Long? = SystemParam.batchNo.get()!!.toLong()): SettlementModel

    @Query("SELECT SUM(CASE WHEN trans_type = 'SALE' THEN 1 ELSE 0 END) AS saleCount, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'REFUND' THEN 1 ELSE 0 END) AS refundCount, SUM(CASE WHEN trans_type = 'REFUND' THEN amount ELSE 0 END) AS refundTotalAmount FROM `Transaction` WHERE batch_no =:batchNo")
    fun getAllSettlementSaleAndRefundCount(
        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
    ): SettlementItemModel

    @Query("SELECT * FROM `Transaction` WHERE trace_no =:traceNo AND trans_type =:transactionType AND payment_channel=:paymentChannel")
    fun getTransactionDetail(
        transactionType: String?,
        traceNo: Long?,
        paymentChannel: String?
    ): TransDataModel

    @Query("SELECT * FROM `Transaction` WHERE trace_no =:traceNo AND payment_channel=:paymentChannel")
    fun getTransactionDetailWithOutType(
        traceNo: Long?,
        paymentChannel: String?
    ): TransDataModel

    @Query("SELECT * FROM `Transaction` WHERE trace_no =:traceNo")
    fun getTransactionDetailWithOutTypeAndChannel(
        traceNo: Long?
    ): TransDataModel


    @Query("SELECT  SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'REFUND' THEN amount ELSE 0 END) AS refundTotalAmount, MAX(batch_no) as batchNo FROM `Transaction` WHERE year =:y AND date =:d")
    fun getYesterdayTransaction(
        y: String = DateUtils.getYesterdayTime("yyyy"),
        d: String = DateUtils.getYesterdayTime("MMdd")
    ): YesterdayTransactionModel


    // SETTLEMENT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(settlement: SettlementDataModel)

    @Query("SELECT * FROM `Settlement` ORDER BY ID DESC LIMIT 1")
    fun getLastSettlement(): SettlementDataModel

    @Query("DELETE FROM `Settlement` WHERE ID in(SELECT MAX(ID) FROM `Settlement`)")
    fun deleteLastSettlement()

    // Suspended QR
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(sus: SuspendedQrDataModel)

    @Query("SELECT * FROM `SuspendedQr` ORDER BY ID DESC LIMIT 1")
    fun getLastSuspendedQr(): SuspendedQrDataModel

    @Query("SELECT * FROM `SuspendedQr` WHERE trace_no =:traceNo LIMIT 1")
    fun getSuspendedQr(traceNo: String): SuspendedQrDataModel

    @Query("SELECT * FROM `SuspendedQr` WHERE status =:status")
    fun getAllSuspendedQr(status: String): List<SuspendedQrDataModel>

    @Query("SELECT * FROM `SuspendedQr` WHERE status =:status ORDER BY id DESC LIMIT :limit OFFSET :offset")
    fun getAllSuspendedQr(
        status: String,
        limit: Int,
        offset: Int
    ): List<SuspendedQrDataModel>

    @Query("DELETE FROM `SuspendedQr` WHERE trace_no =:traceNo")
    fun deleteSuspendedQrWithTraceNo(traceNo: String)

    @Query("UPDATE `SuspendedQr` SET status =:status WHERE trace_no =:traceNo")
    fun updateSuspendQRStatus(traceNo: String, status: String)

    // HISTORY
    @Query("SELECT SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'VOID' THEN amount ELSE 0 END) AS voidTotalAmount FROM `Transaction` WHERE batch_no =:batchNo AND date =:date AND year =:year")
    fun getHistorySummaryByDate(
        batchNo: Long? = SystemParam.batchNo.get()?.toLong(),
        date: String? = DateUtils.getCurrentTime("MMdd"),
        year: String? = DateUtils.getCurrentTime("yyyy")
    ): HistorySummaryByDateModel

    @Query("SELECT payment_channel as paymentChannel, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'VOID' THEN amount ELSE 0 END) AS voidTotalAmount FROM `Transaction` WHERE batch_no =:batchNo AND date =:date AND year =:year AND payment_channel =:paymentChannel")
    fun getHistorySummaryByChannel(
        paymentChannel: String?,
        batchNo: Long? = SystemParam.batchNo.get()?.toLong(),
        date: String? = DateUtils.getCurrentTime("MMdd"),
        year: String? = DateUtils.getCurrentTime("yyyy")
    ): HistorySummaryByChannelModel

    // TRANSACTION HISTORY
//    @Query("SELECT SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount, SUM(CASE WHEN trans_type = 'VOID' THEN amount ELSE 0 END) AS voidTotalAmount FROM `Transaction` WHERE batch_no =:batchNo")
//    fun getSaleAndVoidTodayAllChannel(
//        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
//    ): SettlementItemModel

    @Query("SELECT * FROM `Transaction`  WHERE batch_no =:batchNo GROUP BY mch_order_no ORDER BY id DESC")
    fun getAllTransactionAudit(
        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
    ): List<TransDataModel>?

    @Query("SELECT :paymentChannel as paymentChannel, SUM(CASE WHEN trans_type = 'SALE' THEN 1 ELSE 0 END) AS saleCount, SUM(CASE WHEN trans_type = 'SALE' THEN amount ELSE 0 END) AS saleTotalAmount FROM `Transaction`  WHERE batch_no =:batchNo AND payment_channel =:paymentChannel ORDER BY id DESC")
    fun getSaleCountByChannel(
        paymentChannel: String?,
        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
    ): SaleTotalByChannelModel

    @Query("SELECT * FROM `Transaction`  WHERE payment_channel=:paymentChannel AND batch_no =:batchNo GROUP BY mch_order_no ORDER BY id DESC")
    fun getTransactionAuditByChannel(
        paymentChannel: String?,
        batchNo: Long? = SystemParam.batchNo.get()?.toLong()
    ): List<TransDataModel>?

    @Query("DELETE FROM `Transaction`")
    fun deleteAllTransaction()

    @Query("DELETE FROM `Settlement`")
    fun deleteAllSettlement()

    @Query("DELETE FROM `SuspendedQr`")
    fun deleteAllSuspendQR()
}