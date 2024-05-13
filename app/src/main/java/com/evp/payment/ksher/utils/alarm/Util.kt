package com.evp.payment.ksher.utils.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.evp.payment.ksher.config.ConfigModel
import com.evp.payment.ksher.utils.DateUtils
import com.evp.payment.ksher.utils.sharedpreferences.SharedPreferencesUtil
import com.google.gson.Gson
import timber.log.Timber
import java.util.*


object Util {
    @JvmStatic
    fun alermJob(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent =
            PendingIntent.getBroadcast(context, 19292, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        var configModel: ConfigModel
        val json = SharedPreferencesUtil.getString("config_file", "")
        configModel = Gson().fromJson(json.get().toString(), ConfigModel::class.java)

        val calendar = DateUtils.getNextScheduleInCalendar(
            configModel.data?.setting?.autoSettlementTime ?: "22:00"
        )
//        val calendar = DateUtils.getNextScheduleInCalendar("22:47")
//        calendar.timeInMillis = System.currentTimeMillis()
//        calendar[Calendar.HOUR_OF_DAY] = 23
//        calendar[Calendar.MINUTE] = 20
//        calendar[Calendar.SECOND] = 50

        Timber.d("##### Schedule set to ${calendar.time} #####")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmMgr.setRepeating(
//            AlarmManager.RTC,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            alarmIntent
//        )

        AlarmManagerCompat.setExact(
            alarmMgr,
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
//        alarmMgr.setExact(
//            AlarmManager.RTC_WAKEUP,
//            calendar.timeInMillis,
//            alarmIntent
//        )
    }

    fun postPoneAlarmJob(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent =
            PendingIntent.getBroadcast(context, 19293, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MINUTE, 30)
//        calendar[Calendar.HOUR_OF_DAY] = 22
//        calendar[Calendar.MINUTE] = 7
//        calendar[Calendar.SECOND] = 50

        Timber.d("##### Postpone Schedule set to ${calendar.time} #####")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        alarmMgr.setRepeating(
//            AlarmManager.RTC,
//            calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY,
//            alarmIntent
//        )
        AlarmManagerCompat.setExact(
            alarmMgr,
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            alarmIntent
        )
    }
}