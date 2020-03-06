package org.broadinstitute.clinicapp.api

import com.google.gson.annotations.SerializedName
import io.reactivex.Completable
import io.reactivex.Observable
import okhttp3.OkHttpClient
import org.broadinstitute.clinicapp.Config
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

interface ApiService {

    @POST("token")
    @FormUrlEncoded
    fun grantNewAccessToken(
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("redirect_uri") uri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): Observable<KeycloakToken>

    @POST("token")
    @FormUrlEncoded
    fun refreshAccessToken(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): Observable<KeycloakToken>

    @POST("logout")
    @FormUrlEncoded
    fun logout(
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Completable


    companion object {

        private val okHttpClient = OkHttpClient()
            .newBuilder()
            .build()

        val instance: ApiService by lazy {
            val retrofit = retrofit2.Retrofit.Builder()
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("${Config.keycloakBaseUrl}/")
                .build()
            retrofit.create(ApiService::class.java)
        }
    }
}

data class KeycloakToken(
    @SerializedName("access_token") var accessToken: String? = null,
    @SerializedName("expires_in") var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in") var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token") var refreshToken: String? = null,
    @SerializedName("token_type") var tokenType: String? = null,
    @SerializedName("id_token") var idToken: String? = null,
    @SerializedName("not-before-policy") var notBeforePolicy: Int? = null,
    @SerializedName("session_state") var sessionState: String? = null,
    @SerializedName("token_expiration_date") var tokenExpirationDate: Calendar? = null,
    @SerializedName("refresh_expiration_date") var refreshTokenExpirationDate: Calendar? = null
)