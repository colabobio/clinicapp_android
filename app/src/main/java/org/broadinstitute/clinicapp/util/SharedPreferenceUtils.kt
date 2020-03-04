package org.broadinstitute.clinicapp.util

import android.content.SharedPreferences


const val PREFERENCE_CLINIC_APP = "Preference_clinic_app"

class SharedPreferenceUtils(private val pref: SharedPreferences) {

    fun writeStringToPref(key: String, value: String) {
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun writeBooleanToPref(key: String, value: Boolean) {
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun writeLongToPref(key: String, value: Long) {
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun writeFloatToPref(key: String, value: Float) {
        val editor = pref.edit()
        editor.putFloat(key, value)
        editor.apply()
    }


    fun readStringFromPref(key: String): String? {
        return readStringFromPref(key, "")
    }

    private fun readStringFromPref(key: String, defaultValue: String): String? {
        return pref.getString(key, defaultValue)
    }

    fun readBooleanFromPref(key: String): Boolean {
        return readBooleanFromPref(key, false)
    }

    fun readBooleanFromPref(key: String, defaultValue: Boolean): Boolean {
        return pref.getBoolean(key, defaultValue)
    }

   fun readLongFromPref(key: String): Long {
        return readLongFromPref(key, 0L)
    }

    fun readFloatFromPref(key: String): Float {
        return pref.getFloat(key, 0.0f)
    }

    private fun readLongFromPref(key: String, defaultValue: Long): Long {
        return pref.getLong(key, defaultValue)
    }

    companion object {

        private var INSTANCE: SharedPreferenceUtils? = null

        fun getInstance(sharedPreferences: SharedPreferences): SharedPreferenceUtils  {
            return INSTANCE ?: SharedPreferenceUtils(sharedPreferences)
                .apply { INSTANCE = this }
        }
    }

}