package org.broadinstitute.clinicapp.ui.splash

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.ui.home.HomeActivity


class GoogleSignInActivity : AppCompatActivity() {
        var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null
    lateinit var googleBtn: ImageView

    //    val RC_SIGN_IN = 1000
    lateinit var oneTapClient: SignInClient
    lateinit var signUpRequest: BeginSignInRequest

    // ...
    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true
    // ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_sign_in)

        googleBtn = findViewById(R.id.google_btn)

        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

//        val resultCode = 1
//        val activityResultLauncher = registerForActivityResult (ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == Activity.RESULT_OK) {
//                Log.d("1st message", "I'm here!")
//            }
//        }

        val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult(), ActivityResultCallback {
                    Log.d("Herer", "activity for result")
                    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                        Log.d("Herer2", "activity for result2")
                        Log.d("requestCode", requestCode.toString())
                        Log.d("resultCode", resultCode.toString())
                        Log.d("data", data.toString())
                        Log.d("Herer2", "activity for result2")
                        super.onActivityResult(requestCode, resultCode, data)
                        if (resultCode == Activity.RESULT_OK) {
                            Log.d("1st message", "I'm here!")
                            try {
                                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                                val idToken = credential.googleIdToken
                                Log.d("3rd message", "Gotten your credentials")
                                Log.d("Credentials", credential.toString())
                                Log.d("IDToken", idToken.toString())
                                when {
                                    idToken != null -> {
                                        // Got an ID token from Google. Use it to authenticate
                                        // with your backend.
                                        Log.d(TAG, "Got ID token.")
                                        var email: String = credential.id
                                        Toast.makeText(
                                            applicationContext,
                                            "Email: " + email,
                                            Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                    else -> {
                                        // Shouldn't happen.
                                        Log.d(TAG, "No ID token!")
                                    }
                                }
                            } catch (e: ApiException) {
                                e.printStackTrace()
                            }
                        }
                    }
        }
            )
        //
        googleBtn.setOnClickListener {
            Log.d("1st message", "Button clicked")
            oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        Log.d("2nd message", "Sign up initiated")
                        var intentSenderResult: IntentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
//                        var intentSenderResult = Intent(this, HomeActivity::class.java)
                        activityResultLauncher.launch(intentSenderResult)
                    } catch (e: IntentSender.SendIntentException) {
                        Log.d("2nd message", "Sign up not initiated")
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Log.d("2nd message", "Sign up failed")
                    Log.d(TAG, e.localizedMessage)
                }
        }

    }
}