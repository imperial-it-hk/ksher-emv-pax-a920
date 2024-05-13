package com.evp.payment.ksher.database.repository

import com.evp.payment.ksher.database.DAOAccess
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SuspendedRepository @Inject constructor(
    private val daoAccess: DAOAccess
) {
    // Suspended QR
    suspend fun insertSuspendedQr(
        suspendedQrDataModel: SuspendedQrDataModel
    ): Flow<Result<SuspendedQrDataModel>?> {
        return flow {
            daoAccess.insertData(suspendedQrDataModel)
            emit(querySuspendedLatest())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun querySuspendedQRLatest(
    ): Flow<Result<SuspendedQrDataModel>?> {
        return flow {
            emit(querySuspendedLatest())
        }.flowOn(Dispatchers.IO)
    }

    private fun querySuspendedLatest(): Result<SuspendedQrDataModel>? {
        return daoAccess.getLastSuspendedQr().let {
            Result.success(it)
        }
    }

    suspend fun querySuspendedQR(traceNo: String): Flow<Result<SuspendedQrDataModel>?> {
        return flow {
            emit(Result.success(daoAccess.getSuspendedQr(traceNo)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSuspendedQr(status: String): Flow<Result<List<SuspendedQrDataModel>>?> {
        return flow {
            emit(Result.success(daoAccess.getAllSuspendedQr(status)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSuspendedQr(status: String, limit: Int, offset: Int): Flow<Result<List<SuspendedQrDataModel>>?> {
        return flow {
            emit(Result.success(daoAccess.getAllSuspendedQr(status = status, limit = limit, offset = offset)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteSuspendedQrWithTraceNo(
        traceNo: String
    ): Flow<Result<Any>?> {
        return flow {
            daoAccess.deleteSuspendedQrWithTraceNo(traceNo)
            emit(Result.success(Any()))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun updateSuspendedQrWithTraceNo(
        traceNo: String,
        status: String,
    ): Flow<Result<Any>?> {
        return flow {
            daoAccess.updateSuspendQRStatus(traceNo, status)
            emit(Result.success(Any()))
        }.flowOn(Dispatchers.IO)
    }
}