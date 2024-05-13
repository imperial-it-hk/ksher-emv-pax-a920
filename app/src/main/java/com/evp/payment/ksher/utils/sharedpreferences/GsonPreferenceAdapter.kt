package com.evp.payment.ksher.utils.sharedpreferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.Preference
import com.google.common.base.Strings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Gson preference adapter. Used to serial object.
 * An adapter to save gson to shared preferences using rxpreferences library.
 *
 *
 */
class GsonPreferenceAdapter<T> : Preference.Adapter<T?> {
    private var clazz: Class<T>? = null
    private var type: TypeToken<T>? = null

    /**
     * With class type.
     *
     * @param clazz class.
     */
    constructor(clazz: Class<T>?) {
        this.clazz = clazz
    }

    /**
     * For generic type. No method to parse class. Let the class empty.
     */
    constructor(type: TypeToken<T>?) {
        this.type = type
    }

    override fun get(key: String, preferences: SharedPreferences): T? {
        val str = preferences.getString(key, null)
        if (Strings.isNullOrEmpty(str)) {
            return null
        }

        // If there is class type
        if (clazz != null) {
            return Gson().fromJson(str, clazz)
        }

        // If there is no class type, then use generic type
        return if (type != null) {
            Gson().fromJson(str, type!!.type)
        } else null
    }

    override fun set(key: String, value: T, editor: SharedPreferences.Editor) {
        editor.putString(key, Gson().toJson(value)).apply()
    }
}