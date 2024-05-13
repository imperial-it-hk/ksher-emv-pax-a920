package com.evp.payment.ksher.database.repository

import com.evp.payment.ksher.database.DAOAccess
import com.evp.payment.ksher.database.SettlementItemModel
import com.evp.payment.ksher.database.table.SettlementDataModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SettlementRepository @Inject constructor(
    private val daoAccess: DAOAccess
) {
    suspend fun insertSettlement(
        settlementDataModel: SettlementDataModel
    ): Flow<Result<SettlementDataModel>?> {
        return flow {
            daoAccess.insertData(settlementDataModel)
            emit(querySettlementLatest())
        }.flowOn(Dispatchers.IO)
    }

    private fun querySettlementLatest(): Result<SettlementDataModel>? {
        return daoAccess.getLastSettlement().let {
            Result.success(it)
        }
    }

    suspend fun deleteLastSettlement(
    ) {
        daoAccess.deleteLastSettlement()
    }

    suspend fun getLastSettlement(): Flow<Result<SettlementDataModel>?> {
        return flow {
            emit(querySettlementLatest())
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getSettlementSaleAndRefundCountByChannel(paymentChannel: String): Flow<Result<SettlementItemModel>?> {
        return flow {
            emit(Result.success(daoAccess.getSettlementSaleAndRefundCountByChannel(paymentChannel)))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getAllSettlementSaleAndRefundCount(): Flow<Result<SettlementItemModel>?> {
        return flow {
            emit(Result.success(daoAccess.getAllSettlementSaleAndRefundCount()))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteAllTransaction(): Flow<Result<String>?> {
        return flow {
            daoAccess.deleteAllTransaction()
            emit(Result.success("success"))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteAllSettlement(): Flow<Result<String>?> {
        return flow {
            daoAccess.deleteAllSettlement()
            emit(Result.success("success"))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteAllSuspendQr(): Flow<Result<String>?> {
        return flow {
            daoAccess.deleteAllSuspendQR()
            emit(Result.success("success"))
        }.flowOn(Dispatchers.IO)
    }

}