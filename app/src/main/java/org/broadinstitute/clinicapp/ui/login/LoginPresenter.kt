package org.broadinstitute.clinicapp.ui.login

import android.annotation.SuppressLint
import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Config
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.api.ApiService
import org.broadinstitute.clinicapp.api.ClinicApiService
import org.broadinstitute.clinicapp.api.KeycloakToken
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.User
import org.broadinstitute.clinicapp.data.source.remote.UserResponse
import org.broadinstitute.clinicapp.util.IOAuth2AccessTokenStorage
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils
import org.broadinstitute.clinicapp.util.SharedPreferencesOAuth2Storage
import retrofit2.HttpException
import java.util.*


class LoginPresenter(
    private var view: LoginContract.View,
    val context: Context,
    private val storage: IOAuth2AccessTokenStorage,
    private val preferenceUtils: SharedPreferenceUtils
) :
    LoginContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val api: ApiService = ApiService.instance
    @SuppressLint("CheckResult")
    override fun grantNewAccessToken(
        code: String,
        clientId: String,
        redirectUri: String,
        showLoadingUI: Boolean
    ) {

        if (showLoadingUI) {
            view.showProgress(true)
        }

        api.grantNewAccessToken(code, clientId, redirectUri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(handleSuccess(), handleError())


    }

    override fun logoutUser(clientId: String) {
        storage.getStoredAccessToken()?.refreshToken?.let {
            api.logout(clientId, it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    storage.removeAccessToken()
                    view.reloadLoginPage()
                }
                ) {

                }
        }

    }

    private fun handleSuccess(): Consumer<KeycloakToken> {
        return Consumer { token ->
            val expirationDate = Calendar.getInstance().clone() as Calendar
            val refreshExpirationDate = Calendar.getInstance().clone() as Calendar
            expirationDate.add(Calendar.SECOND, token.expiresIn!!)
            refreshExpirationDate.add(Calendar.SECOND, token.refreshExpiresIn!!)
            token.tokenExpirationDate = expirationDate
            token.refreshTokenExpirationDate = refreshExpirationDate
            storage.storeAccessToken(token)

            view.handleAccessSuccess()
        }
    }

    private fun handleError(): Consumer<Throwable> {
        return Consumer {
            view.showProgress(false)

            when (it) {
                is HttpException -> {
                    val v: HttpException = it
                    if (v.code() == 500) {
                        logoutUser(Config.clientId)
                    }
                }
                }

            view.failedUserCreation()

        }
    }

    @SuppressLint("CheckResult")
    // First login - when user created its values is true, after logout user tries login with same credentials
    // check condition for API hit of create user : check pref ID and response user ID keycloak if same then
    // API should not call, if different then call API of create new user and set new data pref and clear data in DB.
    override fun createNewUser(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        token: String
    ) {
        val api: ClinicApiService = ClinicApiService.create(storage as SharedPreferencesOAuth2Storage)
        val user = User(
            username,
            0,
            firstName,
            lastName,
            email,
            "",
            ""
        )
        view.showProgress(true)
        api.createUser(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(handleUserSuccess(), handleError())

    }

    private fun handleUserSuccess(): Consumer<UserResponse> {
        return Consumer { res ->
            view.showProgress(false)
            ClinicApp.instance?.getPrefStorage()?.writeStringToPref(
                Constants.PrefKey.PREF_FIRST_NAME,
                res.userDetailsdto!!.firstName
            )
            preferenceUtils.writeStringToPref(
                Constants.PrefKey.PREF_LAST_NAME,
                res.userDetailsdto!!.lastName
            )
            preferenceUtils.writeStringToPref(
                Constants.PrefKey.PREF_EMAIL,
                res.userDetailsdto!!.emailId
            )
            preferenceUtils.writeLongToPref(Constants.PrefKey.PREF_USER_ID, res.userDetailsdto!!.id)
            preferenceUtils.writeStringToPref(
                Constants.PrefKey.PREF_USER_NAME,
                res.userDetailsdto!!.keycloakUser
            )
            preferenceUtils.writeStringToPref(
                Constants.PrefKey.PREF_GENDER,
                res.userDetailsdto!!.gender
            )
            preferenceUtils.writeStringToPref(
                Constants.PrefKey.PREF_WORK_LOCATION,
                res.userDetailsdto!!.workLocation
            )
            view.handleUserCreation()

        }
    }

    override fun deleteDataOfPreviousUser(
        username: String,
        email: String,
        firstName: String,
        lastName: String,
        token: String
    ) {
        val repository = ClinicRepository.getInstance(context)
        compositeDisposable.add(repository.deleteFormsAndStudyData()
            .doOnSuccess {
                repository.deleteAllMasterStudyData()
                repository.deleteAllPatients()
                repository.deleteAllFormVariable()

                repository.deleteAllForms()
                repository.deleteAllSyncForms()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                createNewUser(username, email, firstName, lastName, token)
            },
                {
                    it.stackTrace
                }
            ))

    }


    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    override fun attach(view: LoginContract.View) {
        this.view = view
    }
}