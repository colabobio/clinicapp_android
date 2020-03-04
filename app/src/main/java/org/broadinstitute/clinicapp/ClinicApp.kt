package org.broadinstitute.clinicapp

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.gson.Gson
import org.broadinstitute.clinicapp.api.KeycloakToken
import org.broadinstitute.clinicapp.data.RefreshTokenWorker
import org.broadinstitute.clinicapp.util.Cryptography
import org.broadinstitute.clinicapp.util.PREFERENCE_CLINIC_APP
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils
import org.broadinstitute.clinicapp.util.SharedPreferencesOAuth2Storage

class ClinicApp : Application() {

    private var storage: SharedPreferencesOAuth2Storage? = null
    private var pref: SharedPreferenceUtils? = null

    init {
        instance = this
    }

    companion object {
        var instance: ClinicApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun getUserName(): String? {
            return instance?.getPrefStorage()?.readStringFromPref(Constants.PrefKey.PREF_USER_NAME)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        storage =
            SharedPreferencesOAuth2Storage.getInstance(
                getSharedPreferences("ClinicApp", Context.MODE_PRIVATE)!!,
                Gson().also { KeycloakToken::class.java })



        val myConfig = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
        // initialize WorkManager
        WorkManager.initialize(this, myConfig)

        RefreshTokenWorker.startPeriodicRefreshTokenTask(context = this)

        try {
            Cryptography.getInstance()
        }catch (e :Exception){
            e.printStackTrace()
        }
    }

    fun getStorage(): SharedPreferencesOAuth2Storage {
        if (storage == null) {
            storage =
                SharedPreferencesOAuth2Storage.getInstance(
                    applicationContext().getSharedPreferences( "ClinicApp", Context.MODE_PRIVATE ),
                    Gson().also { KeycloakToken::class.java })
        }
        return storage as SharedPreferencesOAuth2Storage
    }

    fun getPrefStorage(): SharedPreferenceUtils {
        if (pref == null) {
            pref =
                SharedPreferenceUtils.getInstance(
                    applicationContext().getSharedPreferences(PREFERENCE_CLINIC_APP, Context.MODE_PRIVATE))
        }
        return pref as SharedPreferenceUtils
    }

}