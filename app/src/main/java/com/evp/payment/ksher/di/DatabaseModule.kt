package com.evp.payment.ksher.di

import android.content.Context
import androidx.room.Room
import com.evp.payment.ksher.database.util.AppDatabase
import com.evp.payment.ksher.database.DAOAccess
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "transaction.db"
        ).build()
    }

    @Provides
    fun provideTransactionDao(appDatabase: AppDatabase): DAOAccess {
        return appDatabase.daoAccess()
    }
}