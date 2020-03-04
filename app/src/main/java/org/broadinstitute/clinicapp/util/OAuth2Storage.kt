package org.broadinstitute.clinicapp.util

import android.content.SharedPreferences
import com.google.gson.Gson
import org.broadinstitute.clinicapp.api.KeycloakToken


interface IOAuth2AccessTokenStorage {
    fun getStoredAccessToken(): KeycloakToken?
    fun storeAccessToken(token: KeycloakToken)
    fun hasAccessToken(): Boolean
    fun removeAccessToken()
}
const val ACCESS_TOKEN_PREFERENCES_KEY = "OAuth2AccessToken"
class SharedPreferencesOAuth2Storage(private val prefs: SharedPreferences, private val g: Gson) : IOAuth2AccessTokenStorage {


    override fun getStoredAccessToken(): KeycloakToken? {
        val tokenStr = prefs.getString(ACCESS_TOKEN_PREFERENCES_KEY, null)
        return if (tokenStr == null) null
        else g.fromJson(tokenStr, KeycloakToken::class.java)
    }

    override fun storeAccessToken(token: KeycloakToken) {
        prefs.edit()
            .putString(ACCESS_TOKEN_PREFERENCES_KEY, g.toJson(token))
            .apply()
    }

    override fun hasAccessToken(): Boolean {
        return prefs.contains(ACCESS_TOKEN_PREFERENCES_KEY)
    }

    override fun removeAccessToken() {
        prefs.edit()
            .remove(ACCESS_TOKEN_PREFERENCES_KEY)
            .apply()
    }

    companion object{
        private var INSTANCE: SharedPreferencesOAuth2Storage? = null

        fun getInstance(sharedPreferences: SharedPreferences, g: Gson): SharedPreferencesOAuth2Storage  {
            return INSTANCE ?: SharedPreferencesOAuth2Storage(sharedPreferences, g)
                .apply { INSTANCE = this }
        }
    }
}
