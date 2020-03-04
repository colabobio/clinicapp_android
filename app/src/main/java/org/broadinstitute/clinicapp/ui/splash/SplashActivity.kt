package org.broadinstitute.clinicapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.broadinstitute.clinicapp.util.TokenHelper


class SplashActivity : BaseActivity() {

    override fun setUp() {
        if (TokenHelper.isRefreshTokenExpired(storage.getStoredAccessToken())) {
            Log.v(this.localClassName, "Refresh token is expired")
        } else {
            Log.v(this.localClassName, "Refresh token is valid")
        }

    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUp()

        Handler().postDelayed({
            intent = if (!storage.hasAccessToken() ||(TokenHelper.isTokenExpired(storage.getStoredAccessToken()) && isNetworkConnected)) {
                Intent(this, LoginActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
                      .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_SPLASH)
            }
            startActivity(intent)
            finish()
        }, 2000)
    }

   }
