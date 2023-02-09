package org.broadinstitute.clinicapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.broadinstitute.clinicapp.util.TokenHelper


class SplashActivity : BaseActivity() {


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


//        Handler().postDelayed({
//            intent = if (!storage.hasAccessToken() ||(TokenHelper.isTokenExpired(storage.getStoredAccessToken()) && isNetworkConnected)) {
//                Intent(this, LoginActivity::class.java)
//            } else {
//                Intent(this, HomeActivity::class.java)
//                      .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_SPLASH)
//            }
//            startActivity(intent)
//            finish()
//        }, 2000)

        Handler().postDelayed({
            pref.writeBooleanToPref(Constants.PrefKey.PREF_USER_CREATED, true)
//            intent = Intent(this, HomeActivity::class.java)
            intent = Intent(this, GoogleSignInActivity::class.java)
//                .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_LOGIN)
            startActivity(intent)
            finish()
        }, 2000)


    }

    override fun setUp() {
        TODO("Not yet implemented")
    }

}
