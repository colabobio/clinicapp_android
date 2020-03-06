package org.broadinstitute.clinicapp.ui.login

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.webkit.*
import org.broadinstitute.clinicapp.Config
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.PrefKey.PREF_USER_CREATED
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.TokenHelper.parseJwtToken

class LoginActivity : BaseActivity(), LoginContract.View {

    private lateinit var myWebView: WebView
    private lateinit var presenter: LoginContract.Presenter
    private val authCodeUrl = Uri.parse(Config.authenticationCodeUrl)
        .buildUpon()
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("client_id", Config.clientId)
       // .appendQueryParameter("scope", "openid")
        .appendQueryParameter("scope", "openid info offline_access")
        .appendQueryParameter("redirect_uri", Config.redirectUri)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        myWebView = findViewById(R.id.webView)
        myWebView.clearHistory()
        myWebView.clearCache(true)
       // myWebView.settings.javaScriptEnabled = true
        myWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        presenter = LoginPresenter(
            this,
            this,storage,pref
        )
        presenter.attach(this)
        myWebView.webViewClient = MyWebViewClient(this)

        if (isNetworkConnected) myWebView.loadUrl(authCodeUrl.toString())
        else onError(R.string.network_error)

    }

    override fun showProgress(show: Boolean) {
        if (show) showLoading()
        else hideLoading()
    }

    @SuppressLint("CheckResult")
    fun authenticate(uri: Uri?) {
        if (uri != null && uri.toString().startsWith(Config.redirectUri)) {
            val code = uri.getQueryParameter("code")
           // myWebView.loadUrl("about:blank")
            code?.let { presenter.grantNewAccessToken(
                code = it,
                clientId = Config.clientId,
                redirectUri = Config.redirectUri,
                showLoadingUI = false
            ) }
        }
    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    override fun setUp() {

    }

    override fun handleAccessSuccess() {
        showData()
    }

    override fun handleUserCreation() {
        pref.writeBooleanToPref(PREF_USER_CREATED, true)
        intent = Intent(this, HomeActivity::class.java)
        .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_LOGIN)
        startActivity(intent)
        finish()
    }

    override fun deletedData(username: String, email: String, firstName: String, lastName: String, token: String) {

        presenter.createNewUser(username, email, firstName, lastName, token)
    }
    override fun handleDifferentUserLogin(username :String, email:String, firstName:String,  lastName:String, token :String) {
        CommonUtils.showDialog(this, getString(R.string.warning) , getString(R.string.login_different_user),
            getString(R.string.continue_str), getString(R.string.cancel),
            object : CommonUtils.DialogCallback {
                override fun positiveClick() {
                    myWebView.clearCache(true)
                    presenter.deleteDataOfPreviousUser(username, email, firstName, lastName, token)
                }

                override fun negativeClick() {

                    pref.writeStringToPref(Constants.PrefKey.PREF_ACCESS_TOKEN, "")
                    // Clear login page and show new empty login page
                    val t = storage.getStoredAccessToken()?.refreshToken
                    if(t!=null) { presenter.logoutUser(Config.clientId) }
                    else reloadLoginPage()
                }
            })
    }

    override fun reloadLoginPage() {
        myWebView.loadUrl(authCodeUrl.toString())
    }

    override fun failedUserCreation() {
        pref.writeBooleanToPref(PREF_USER_CREATED, false)
        CommonUtils.showDialog(this, getString(R.string.error) , getString(R.string.login_error),
            getString(R.string.ok), "",
            object : CommonUtils.DialogCallback {
            override fun positiveClick() {
                storage.removeAccessToken()
                myWebView.clearCache(true)
                Handler().postDelayed({
                    finish()
                }, 1000)
            }

            override fun negativeClick() {

            }
        })

    }

    @SuppressLint("CheckResult", "SetTextI18n")
    private fun showData() {
        val token = storage.getStoredAccessToken()
        token?.apply {
            val principal = parseJwtToken(accessToken!!)

            val lastLoginUser = pref.readStringFromPref(Constants.PrefKey.PREF_USER_NAME)

            val userName = principal.preferred_username.toString()
            val name = principal.name.toString()
            val surname = principal.surname.toString()
            val email = principal.email.toString()
            pref.writeStringToPref(Constants.PrefKey.PREF_ACCESS_TOKEN, token.accessToken.toString())
            expiresIn?.let { pref.writeLongToPref(Constants.PrefKey.PREF_ACCESS_TOKEN_EXPIRES, it.toLong()) }


            if(lastLoginUser!= null && lastLoginUser.isNotBlank()){
                if (lastLoginUser == userName && pref.readBooleanFromPref(
                        PREF_USER_CREATED)) {
                    // Don't call API for create new USER & Navigate to Home screen
                  handleUserCreation()
                }else if(lastLoginUser != userName) {
                    handleDifferentUserLogin(userName, email, name, surname, token.accessToken.toString())
                    // On Continue button hit createNewUser
                }
            }
            else {
                presenter.createNewUser(userName, email, name, surname, token.accessToken.toString())
            }
        }
    }

    class MyWebViewClient(private val loginActivity: LoginActivity) : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (Uri.parse(url).host == "10.0.2.2" ||  (Uri.parse(url).host == Config.host)) {
                return false
            }else
                if (url != null && url.toString().startsWith(Config.redirectUri)) {
                    val code = Uri.parse(url).getQueryParameter("code")
                    loginActivity.authenticate(Uri.parse(url))
                }
            return true
        }

        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
          // handleError(view, error?.errorCode, error?.description.toString(), request?.url)
        }


//        private fun handleError(view: WebView?, errorCode: Int?, description: String?, uri: Uri?) {
//          //  val host = uri?.host// e.g. "google.com"
//          //  val scheme = uri?.scheme// e.g. "https"
//            Log.v("handleError" , "loading URL has some error")
//           // view?.loadUrl("about:blank")
//        }
    }

}
