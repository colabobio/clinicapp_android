package org.broadinstitute.clinicapp.api

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.broadinstitute.clinicapp.Config
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.User
import org.broadinstitute.clinicapp.data.source.remote.*
import org.broadinstitute.clinicapp.util.SharedPreferencesOAuth2Storage
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ClinicApiService {

    @Headers("Content-Type: application/json;charset=UTF-8")
    @GET("api/master-variables")
    fun getMasterVariables(
        @Query("lastModified") lastModified: Long,
        @Query("size") pageSize: Int,
        @Query("page") pageNo: Int
    ): Single<MasterVariablesResponse>

    @GET("api/master-study-forms/lookup")
    fun searchStudyForms(
        @Query("search") searchTerm: String,
        @Query("size") pageSize: Int,
        @Query("page") pageNo: Int,
        @Query("excludeUser") excludeUserId: String?
    ): Single<StudyFormsResponse>

    @GET("api/master-study-forms/{user-id}")
    fun getMyStudyForms(
        @Path(value = "user-id", encoded = true) userId: String,
        @Query("lastModified") lastModified: Long,
        @Query("size") pageSize: Int,
        @Query("page") pageNo: Int
    ): Single<StudyFormsResponse>

    @GET("api/master-study-data")
    fun getStudyDataByFormId(
        @Query(value = "user") userId: String,
        @Query("formId") studyFormId: String,
        @Query("size") pageSize: Int,
        @Query("page") pageNo: Int
    ): Single<StudyDataResponse>

    @GET("api/master-study-data/user")
    fun getStudyAllData(
        @Query(value = "user") userId: String,
        @Query("lastModified") lastModified: Long,
        @Query("size") pageSize: Int,
        @Query("page") pageNo: Int
    ): Single<StudyDataResponse>

    @POST("api/master-study-data")
    fun submitStudyData(@Body body: StudyDataRequest): Single<CreateStudyFormsResponse>

    @POST("api/user-details")
    fun createUser(@Body user: User): Observable<UserResponse>

    @POST("api/master-study-forms")
    fun submitStudyForms(@Body body: List<MasterStudyForms>): Single<CreateStudyFormsResponse>

    @POST("api/study-form-associations")
    fun associateStudyForm(@Body userId: JsonObject): Single<CreateStudyFormsResponse>

    class OAuthInterceptor(private val tokenType: String) :
        Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
//            val accessToken = storage?.getStoredAccessToken()?.accessToken.toString()
            val accessToken = "no token"
            request =
                request.newBuilder().header("Authorization", "$tokenType $accessToken").build()

            return chain.proceed(request)
        }
    }


    companion object {
        private var storage: SharedPreferences? = null
        fun create(prefs: SharedPreferences?): ClinicApiService {
            storage = prefs

            val client = OkHttpClient.Builder()
                .addInterceptor(OAuthInterceptor("Bearer"))
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val input = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

            val retrofit = retrofit2.Retrofit.Builder()
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(input))
                .baseUrl("${Config.apiBaseUrl}/")
                .build()

            return retrofit.create(ClinicApiService::class.java)
        }
    }
}