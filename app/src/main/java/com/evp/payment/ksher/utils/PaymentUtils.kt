//package com.evp.payment.ksher.utils
//
//import com.evp.payment.ksher.database.repository.TransactionRepository
//import com.evp.payment.ksher.utils.constant.KsherConstant
//import com.ksher.ksher_sdk.Ksher_pay_sdk
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.flow.collect
//import kotlin.coroutines.CoroutineContext
//
//object PaymentUtils: CoroutineScope {
//
//    private val job = Job()
//    override val coroutineContext: CoroutineContext = job + Dispatchers.IO
//
//    suspend fun selectAppId(paymentChannel: String, ksherPaySdk: Ksher_pay_sdk, transactionRepository: TransactionRepository){
//         transactionRepository.getTransactionByPaymentChannelLatest(paymentChannel).collect {
//            if (KsherConstant.appid == it?.getOrNull()?.appid) {
//                ksherPaySdk.UpdateAppId(KsherConstant.appid, KsherConstant.privateKey)
//            } else {
//                ksherPaySdk.UpdateAppId(
//                    KsherConstant.appid_offline,
//                    KsherConstant.privateKey_offline
//                )
//            }
//        }
//    }
//}