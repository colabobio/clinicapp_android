package org.broadinstitute.clinicapp.ui.login

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity

import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import org.broadinstitute.clinicapp.ClinicApp

class LoginActivity : BaseActivity() {
    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUp()
    }

    override fun setUp() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(org.broadinstitute.clinicapp.BuildConfig.web_client_id)
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }


    fun signIn(view: View) {
        // Signing out just in case the user is returning from the home screen
        oneTapClient.signOut()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("ClinicApp signIn", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d("ClinicApp signIn", e.localizedMessage)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            Log.d("ClinicApp signIn", "Got ID token.")
                        }
                        password != null -> {
                            // Got a saved username and password. Use them to authenticate
                            // with your backend.
                            Log.d("ClinicApp signIn", "Got password.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d("ClinicApp signIn", "No ID token or password!")
                        }
                    }

                    pref  =  ClinicApp.instance!!.getPrefStorage()
                    pref.writeStringToPref(Constants.PrefKey.PREF_USER_NAME, username)
                    pref.writeBooleanToPref(Constants.PrefKey.PREF_USER_CREATED, true)

                    handleUserLogin()
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.d("ClinicApp signIn", "One-tap dialog was closed.")
                            // Don't re-prompt the user.
                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.d("ClinicApp signIn", "One-tap encountered a network error.")
                            // Try again or just ignore.
                        }
                        else -> {
                            Log.d("ClinicApp signIn", "Couldn't get credential from result." +
                                    " (${e.localizedMessage})")
                        }
                    }
                }
            }
        }
    }


    fun handleUserLogin() {
        intent = Intent(this, HomeActivity::class.java)
            .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_LOGIN)
        startActivity(intent)
    }
}
