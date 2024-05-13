package com.evp.payment.ksher.database.util

import androidx.room.Database
import androidx.room.RoomDatabase
import com.evp.payment.ksher.database.DAOAccess
import com.evp.payment.ksher.database.table.SettlementDataModel
import com.evp.payment.ksher.database.table.SuspendedQrDataModel
import com.evp.payment.ksher.database.table.TransDataModel

@Database(
    version = 1,
    entities = [
        TransDataModel::class,
        SettlementDataModel::class,
        SuspendedQrDataModel::class],
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daoAccess(): DAOAccess
}