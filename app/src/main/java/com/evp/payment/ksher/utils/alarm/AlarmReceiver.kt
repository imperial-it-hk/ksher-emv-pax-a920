package com.evp.payment.ksher.utils.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.evp.payment.ksher.database.repository.SettlementRepository
import com.evp.payment.ksher.function.settlement.AutoSettlementActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settlementRepository: SettlementRepository

    override fun onReceive(context: Context, intent: Intent) {
//        showPushNotification() // implement showing notification in this function

        Timber.d("######### AlarmReceiver called #########")
        CoroutineScope(Dispatchers.IO).launch {
            settlementRepository.getAllSettlementSaleAndRefundCount().collect {
                if (it?.getOrNull()?.saleTotalAmount ?: 0 == 0 && it?.getOrNull()?.refundTotalAmount ?: 0 == 0) {
                    Timber.d("######### AlarmReceiver  No transaction found")
                } else {
                    Timber.d("######### AlarmReceiver transaction found")
                    context.startActivity(Intent(context, AutoSettlementActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }
                delay(1000)
                Util.alermJob(context)
            }
        }


//        val notificationManager =
//            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        val pendingIntent =
//            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        notification =
//            Notification(R.drawable.alarmicon, "charSequence", System.currentTimeMillis())
//
//        notification.setLatestEventInfo(context, "alarmTitle", "charSequence", pendingIntent)
//
//        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
//
//        notificationManager.notify(1, notification)
    }
}