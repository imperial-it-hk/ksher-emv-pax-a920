package com.evp.payment.ksher.database.repository

import com.evp.payment.ksher.database.*
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val daoAccess: DAOAccess
) {
    suspend fun insertTransaction(
        transDataModel: TransDataModel
    ): Flow<Result<TransDataModel>?> {
        return flow {
            daoAccess.insertData(transDataModel)
            emit(queryTransactionLatest())
        }.flowOn(Dispatchers.IO)
    }


    suspend fun addAndUpdate(
        transData: TransDataModel,
        origTransData: TransDataModel
    ): Flow<Result<TransDataModel>?> {
        return flow {
            daoAccess.insertData(transData)
            daoAccess.insertData(origTransData)
            emit(queryTransactionLatest())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTransactionLatest(): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryTransactionLatest())
        }.flowOn(Dispatchers.IO)
    }

    private fun queryTransactionLatest(): Result<TransDataModel>? {
        return daoAccess.getLastTransaction().let {
            Result.success(it)
        }
    }

    suspend fun getTransactionByPaymentChannelLatest(paymentChannel: String): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryTransactionPaymentChannelLatest(paymentChannel))
        }.flowOn(Dispatchers.IO)
    }

    private fun queryTransactionPaymentChannelLatest(paymentChannel: String): Result<TransDataModel>? {
        return daoAccess.getLastTransactionByChannel(paymentChannel).let {
            Result.success(it)
        }
    }


    suspend fun getTransactionDetailLatest(mchOrderNo: String): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryTransactionDetailLatest(mchOrderNo))
        }.flowOn(Dispatchers.IO)
    }

    private fun queryTransactionDetailLatest(mchOrderNo: String): Result<TransDataModel>? {
        return daoAccess.getTransactionDetailLatest(mchOrderNo).let {
            Result.success(it)
        }
    }

    suspend fun getTransactionDetailByOriginalTraceNo(
        oriTraceNo: String
    ): Flow<Result<TransDataModel>?> {
        return flow {
            emit(daoAccess.getTransactionDetailByOriginalTraceNo(oriTraceNo).let {
                Result.success(it)
            })
        }.flowOn(Dispatchers.IO)
    }


    suspend fun getTransactionDetail(
        traceNo: String
    ): Flow<Result<TransDataModel>?> {
        return flow {
            emit(daoAccess.getTransactionDetail(traceNo).let {
                Result.success(it)
            })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getTransactionDetail(
        transType: String,
        traceNo: String
    ): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryTransactionDetail(transType, traceNo))
        }.flowOn(Dispatchers.IO)
    }

    private fun queryTransactionDetail(
        transType: String,
        traceNo: String
    ): Result<TransDataModel>? {
        return daoAccess.getTransactionDetail(transType, traceNo).let {
            Result.success(it)
        }
    }

    suspend fun getAllTransaction(
        limit: Int = 100,
        offset: Int
    ): Flow<Result<TransactionResponse>?> {
        return flow {
            emit(fetchTransactionCached(limit, offset))
        }.flowOn(Dispatchers.IO)
    }

    private fun fetchTransactionCached(limit: Int, offset: Int): Result<TransactionResponse>? =
        daoAccess.getAllTransaction(limit, offset)?.let {
            Result.success(TransactionResponse(it))
        }

    suspend fun getAllTransactionWithChannel(
        channel: String,
        limit: Int = 100,
        offset: Int
    ): Flow<Result<TransactionResponse>?> {
        return flow {
            emit(fetchTransactionWithChannelCached(channel, limit, offset))
        }.flowOn(Dispatchers.IO)
    }

    private fun fetchTransactionWithChannelCached(
        channel: String,
        limit: Int,
        offset: Int
    ): Result<TransactionResponse>? =
        daoAccess.getAllTransactionWithPaymentChannel(channel, limit, offset)?.let {
            Result.success(TransactionResponse(it))
        }

    suspend fun deleteTransaction(mchOrderNo: String, position: Int): Flow<Result<Int>?> {
        return flow {
            daoAccess.deleteTransaction(mchOrderNo)
            emit(Result.success(position))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAnyTransaction(
        traceNo: Long,
    ): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryAnyTransaction(traceNo))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAnyTransaction(
        traceNo: Long,
        paymentChannel: String
    ): Flow<Result<TransDataModel>?> {
        return flow {
            emit(queryAnyTransaction(traceNo, paymentChannel))
        }.flowOn(Dispatchers.IO)
    }

    private fun queryAnyTransaction(
        traceNo: Long
    ): Result<TransDataModel>? {
        return daoAccess.getTransactionDetailWithOutTypeAndChannel(traceNo).let {
            Result.success(it)
        }
    }

    private fun queryAnyTransaction(
        traceNo: Long,
        paymentChannel: String
    ): Result<TransDataModel>? {
        return daoAccess.getTransactionDetailWithOutType(traceNo, paymentChannel).let {
            Result.success(it)
        }
    }

    suspend fun queryHistorySummaryByDate(): Flow<Result<HistorySummaryByDateModel>?> {
        return flow {
            emit(Result.success(daoAccess.getHistorySummaryByDate()))
        }
    }

    suspend fun queryHistorySummaryByChannel(paymentChannel: String): Flow<Result<HistorySummaryByChannelModel>?> {
        return flow {
            emit(Result.success(daoAccess.getHistorySummaryByChannel(paymentChannel)))
        }
    }

    suspend fun getSettlementSaleAndRefundCountByChannel(paymentChannel: String): Flow<Result<SettlementItemModel>?> {
        return flow {
            emit(Result.success(daoAccess.getSettlementSaleAndRefundCountByChannel(paymentChannel)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getReportAuditAllChannel(): Flow<Result<TransactionResponse>?> {
        return flow {
            emit(Result.success(TransactionResponse(daoAccess.getAllTransactionAudit())))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSaleCountByChanel(paymentChannel: String): Flow<Result<SaleTotalByChannelModel>?> {
        return flow {
            emit(Result.success(daoAccess.getSaleCountByChannel(paymentChannel)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getReportAuditByChannel(paymentChannel: String): Flow<Result<TransactionResponse>?> {
        return flow {
            emit(
                Result.success(
                    TransactionResponse(
                        daoAccess.getTransactionAuditByChannel(
                            paymentChannel
                        )
                    )
                )
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getYesterdayTransaction(): Flow<Result<YesterdayTransactionModel>?> {
        return flow {
            emit(Result.success(daoAccess.getYesterdayTransaction()))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteTransactionCircular(maxRowToKeep: Int): Flow<Result<Int>?> {
        return flow {
            daoAccess.deleteTransactionCircular(maxRowToKeep)
            daoAccess.deleteSuspendedQrCircular(maxRowToKeep)
            emit(Result.success(0))
        }.flowOn(Dispatchers.IO)
    }

//    suspend fun getAllSettlementSaleAndRefundCount(): Flow<Result<SettlementItemModel>?> {
//        return flow {
//            emit(Result.success(daoAccess.getAllSettlementSaleAndRefundCount()))
//        }.flowOn(Dispatchers.IO)
//    }
}