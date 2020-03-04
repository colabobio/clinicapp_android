package org.broadinstitute.clinicapp.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.*
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Config
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.api.ApiService
import org.broadinstitute.clinicapp.api.KeycloakToken
import org.broadinstitute.clinicapp.util.*
import org.broadinstitute.clinicapp.util.TokenHelper.formatDate
import org.json.JSONObject
import retrofit2.HttpException
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RefreshTokenWorker(context: Context, params: WorkerParameters): Worker(context, params) {

    private val api: ApiService
    private val storage: SharedPreferencesOAuth2Storage

    companion object {

        fun startPeriodicRefreshTokenTask(context: Context) {
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val expiresIn = ClinicApp.instance?.getPrefStorage()?.readLongFromPref(Constants.PrefKey.PREF_ACCESS_TOKEN_EXPIRES)
            var interval: Long
            if (expiresIn != null && expiresIn > 0) {
                interval = (expiresIn/60)/2
                Log.e("interval :", "interval ::$interval expiresIn  $expiresIn" )
                if(interval < 16) interval = 15
                val periodicWork =
                    PeriodicWorkRequest.Builder(RefreshTokenWorker::class.java, interval, MINUTES)
                        .setConstraints(workConstraints)
                        .setInitialDelay(5, MINUTES)
                        .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork("clinicApp-refresh-token-work", REPLACE, periodicWork)
            }
        }

        const val channelId = "clinicApp_channel_id"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Clinic notification channel", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        storage = ClinicApp.instance?.getStorage()!!
        api = ApiService.instance
    }


    override fun doWork(): Result {

        if (storage.getStoredAccessToken() == null || storage.getStoredAccessToken()!!.refreshToken == null)
            return Result.failure()

        return try {
            if(NetworkUtils.isNetworkConnected(applicationContext)){
                Log.v("RefreshTokenWorker", "doWork")
                saveTokenToStorage(api.refreshAccessToken(storage.getStoredAccessToken()!!.refreshToken!!, Config.clientId)
                    .blockingFirst())

            }
                Result.success()

        } catch (e: Exception) {
            //{"error":"invalid_grant","error_description":"Offline user session not found"}

            if(e is HttpException){
                if(e.code() == 400){
                    val body = e.response().errorBody()
                  //  Log.v("body", body?.string())
                    val mainObject = JSONObject(body?.string())
                    val error = mainObject.optString("error")
                    if(error.contains("invalid_grant")){
                        // Say to login user and expire refresh token or offline access
                        storage.removeAccessToken()
                    }

                }
            }
            Result.failure()//retry()

        }
    }

    private fun saveTokenToStorage(token: KeycloakToken): KeycloakToken {
        val expirationDate = Calendar.getInstance().clone() as Calendar
        val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
        expirationDate.add(Calendar.SECOND, token.expiresIn!!)
        refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
        token.tokenExpirationDate = expirationDate
        token.refreshTokenExpirationDate = refreshExpirationDate
        storage.storeAccessToken(token)
        val pref = SharedPreferenceUtils.getInstance(
            ClinicApp.applicationContext().getSharedPreferences(PREFERENCE_CLINIC_APP, Context.MODE_PRIVATE))
        pref.writeStringToPref(Constants.PrefKey.PREF_ACCESS_TOKEN, token.accessToken.toString())
        pref.writeLongToPref(Constants.PrefKey.PREF_ACCESS_TOKEN_EXPIRES,  token.expiresIn!!.toLong())

        token.apply {
            val principal = TokenHelper.parseJwtToken(token.accessToken)

            val text =
                "user: ${principal.name} ${principal.surname} (${principal.email})\n" +
                    //   "id: ${principal.userId}\n"
                    //  "available roles: ${principal.roles.joinToString(", ")}\n\n" +
                    "token expires in: $expiresIn sec (${tokenExpirationDate!!.formatDate()})\n" +
                    "refresh expires in: $refreshExpiresIn sec (${refreshTokenExpirationDate!!.formatDate()})\n\n" +
                    "token: $accessToken\n\n" +
                    "refreshToken: $refreshToken"
            Log.v("Tag", "showDa$text")
        }

        return token
    }
}