package com.evp.payment.ksher.utils

import android.annotation.SuppressLint
import com.evp.eos.utils.LogUtil
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object DateUtils {
    private const val TAG = "DateUtils"

    const val format_date_time = "yyyy-mm-dd HH:mm:ss"

    @SuppressLint("WrongConstant")
    fun getMonthesBetween(beginDate: Date?, endDate: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = beginDate
        val month1 = calendar[1] * 12 + calendar[2]
        calendar.time = endDate
        val month2 = calendar[1] * 12 + calendar[2]
        return month2 - month1 + 1
    }

    @SuppressLint("WrongConstant")
    fun getMonthOfYear(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[2]
    }

    fun getDate(year: Int, month: Int, day: Int): Date {
        return getDate(year, month, day, 0, 0, 0)
    }

    fun getDate(year: Int, month: Int, day: Int, hourOfDay: Int, minute: Int, second: Int): Date {
        val calendar = Calendar.getInstance()
        calendar[year, month, day, hourOfDay, minute] = second
        return calendar.time
    }

    @SuppressLint("WrongConstant")
    fun getYear(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[1]
    }

    @get:SuppressLint("WrongConstant")
    val year: Int
        get() {
            val calendar = Calendar.getInstance()
            return calendar[1]
        }

    @SuppressLint("WrongConstant")
    fun getMonth(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[2]
    }

    @SuppressLint("WrongConstant")
    fun getDaysOfMonth(date: Date?): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.getActualMaximum(5)
    }

    @SuppressLint("WrongConstant")
    fun getDayOfMonth(date: Date?): Long {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar[5].toLong()
    }

    fun getDaysBetween(beginDate: Date, endDate: Date): Long {
        return (endDate.time - beginDate.time) / 86400000L
    }

    fun getDate(date: Date?, expectedFormat: String?): String {
        val format = SimpleDateFormat(expectedFormat)
        return format.format(date)
    }

    @SuppressLint("WrongConstant")
    fun getDayOfWeek(date: Date?): Int {
        val c = Calendar.getInstance()
        c.time = date
        return c[7]
    }

    fun getDate(value: String?, format: String?): Date? {
        var date: Date? = null
        try {
            date = SimpleDateFormat(format).parse(value)
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        return date
    }

    fun getNextScheduleInMilliseconds(value: String?): Long {
        val times = value?.split(":")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, times!![0].toInt())
        calendar.set(Calendar.MINUTE, times[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        val curDate = Date(System.currentTimeMillis())
        if (curDate.after(calendar.time)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val curC = Calendar.getInstance().apply {
            time = curDate
        }

        return calendar.timeInMillis - curC.timeInMillis
    }

    fun getNextScheduleInCalendar(value: String?): Calendar {
        val times = value?.split(":")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, times!![0].toInt())
        calendar.set(Calendar.MINUTE, times[1].toInt())
        calendar.set(Calendar.SECOND, 0)

        val curDate = Date(System.currentTimeMillis())
        if (curDate.after(calendar.time)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar
    }


    fun getCurrentTime(format: String?): String {
        val df = SimpleDateFormat(format)
        val curDate = Date(System.currentTimeMillis())
        return df.format(curDate)
    }

    fun getYesterdayTime(format: String?): String {
        val df = SimpleDateFormat(format)
        val yesDate = getDateBefore(Date(System.currentTimeMillis()), 1)
        return df.format(yesDate)
    }

    fun getDaysBetween(beginDay: String?, endDay: String?, format: String?): Long {
        val df = SimpleDateFormat(format)
        var to = 0L
        try {
            to = df.parse(beginDay).time
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        var from = 0L
        try {
            from = df.parse(endDay).time
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        return (from - to) / 86400000L
    }

    @SuppressLint("WrongConstant")
    fun getCalendarOfInterval(nowDate: Date?, days: Int): Calendar {
        val ca2 = Calendar.getInstance()
        ca2.time = nowDate
        ca2.add(5, days)
        return ca2
    }

    fun getFormattedDate(date: String?, oldFormat: String?, newFormat: String?): String? {
        return try {
            SimpleDateFormat(newFormat).format(SimpleDateFormat(oldFormat).parse(date))
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
            null
        }
    }

    fun getFormattedTime(time: String?, oldFormat: String?, newFormat: String?): String? {
        return try {
            SimpleDateFormat(newFormat).format(SimpleDateFormat(oldFormat).parse(time))
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
            null
        }
    }

    fun getFormattedDateTime(dataTime: String?, oldFormat: String?, newFormat: String?): String? {
        return try {
            SimpleDateFormat(newFormat).format(SimpleDateFormat(oldFormat).parse(dataTime))
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
            null
        }
    }

    fun getFormattedDateTime(dataTime: String?, oldFormat: String?): Date? {
        return try {
            SimpleDateFormat(oldFormat).parse(dataTime)
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
            null
        }
    }

    fun getSecondsBetween(beginTime: String?, endTime: String?, format: String?): Long {
        val df = SimpleDateFormat(format)
        var to = 0L
        try {
            to = df.parse(beginTime).time
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        var from = 0L
        try {
            from = df.parse(endTime).time
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        return (from - to) / 1000L
    }

    @SuppressLint("WrongConstant")
    fun getDateBefore(d: Date?, day: Int): Date {
        val now = Calendar.getInstance()
        now.time = d
        now[5] = now[5] - day
        return now.time
    }

    @SuppressLint("WrongConstant")
    fun compareDate(begin: Date?, end: Date?, day: Int): Boolean {
        val cbegin = Calendar.getInstance()
        cbegin.time = begin
        val cend = Calendar.getInstance()
        cend.time = end
        return cend[5] - cbegin[5] <= day
    }

    fun compareDate(date1: String?, date2: String?, format: String?): Boolean {
        val df = SimpleDateFormat(format)
        try {
            val dt1 = df.parse(date1)
            val dt2 = df.parse(date2)
            if (dt1.time < dt2.time) {
                return true
            }
        } catch (e: ParseException) {
            LogUtil.e(TAG, e)
        }
        return false
    }

    fun isValidDate(date: String?, fromat: String?): Boolean {
        return try {
            val dateFormat = SimpleDateFormat(fromat)
            dateFormat.isLenient = false
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            LogUtil.e(TAG, e)
            false
        }
    }

    fun isValidTime(time: String?, format: String?): Boolean {
        val eL = "^([0-1][0-9]|[2][0-3])([0-5][0-9])([0-5][0-9])"
        val p = Pattern.compile(eL)
        val m = p.matcher(time)
        return m.matches()
    }

    fun Calendar.getAbbreviatedFromDateTime(field: String): String? {
        val output = SimpleDateFormat(field)
        try {
            return output.format(time)    // format output
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}