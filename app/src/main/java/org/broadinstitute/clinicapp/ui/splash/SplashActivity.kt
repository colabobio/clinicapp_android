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
//    var gso: GoogleSignInOptions? = null
//    var gsc: GoogleSignInClient? = null

    override fun setUp() {
//        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
//        if (account != null){
//            Log.d("accountId", account.id.toString())
//            Log.d("accountId", account.idToken.toString())
//            Log.d("accountId", account.displayName.toString())
//            Log.d("accountId", account.email .toString())
//            Log.d("accountId", account.familyName.toString())
//            Log.d("accountId", account.givenName.toString())
//            Log.d("accountId", account.serverAuthCode.toString())
//        // Signed in successfully, show authenticated UI.
//    }

    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

//        // Configure sign-in to request the user's ID, email address, and basic
//        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        // Configure sign-in to request the user's ID, email address, and basic
//        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
//        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .build()
//        gsc = GoogleSignIn.getClient(this, gso!!);

        setUp()

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
            intent = Intent(this, HomeActivity::class.java)
                .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_LOGIN)
            startActivity(intent)
            finish()
        }, 2000)


    }

   }
