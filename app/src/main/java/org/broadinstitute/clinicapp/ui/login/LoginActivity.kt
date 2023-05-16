package org.broadinstitute.clinicapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.DriveServiceHelper
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import java.util.*

class LoginActivity : BaseActivity() {

    private val TAG = "MainActivity"
    private val REQUEST_CODE_SIGN_IN = 1

    companion object {
        var client: GoogleSignInClient? = null
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()

        var button = findViewById<SignInButton>(R.id.sign_in_button)
        button.setOnClickListener {
            setUp()
        }
    }

    override fun setUp() {
        Log.d(TAG, "Requesting sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()
        client = GoogleSignIn.getClient(this, signInOptions)

        startActivityForResult(client!!.signInIntent, REQUEST_CODE_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun handleSignInResult(result: Intent) {

        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Log.d(TAG, "Signed in as " + googleAccount.email)

                try {
                    val id = googleAccount.id
                    val email = googleAccount.email
                    val firstName = googleAccount.givenName
                    val familyName = googleAccount.familyName


                    pref = ClinicApp.instance!!.getPrefStorage()
                    if (id != null) {
                        pref.writeStringToPref(Constants.PrefKey.PREF_USER_NAME, id)
                        pref.writeBooleanToPref(Constants.PrefKey.PREF_USER_CREATED, true)
                    }

                    if (email != null) {
                        pref.writeStringToPref(Constants.PrefKey.PREF_EMAIL, email)
                    }

                    if (firstName != null) {
                        pref.writeStringToPref(Constants.PrefKey.PREF_FIRST_NAME, firstName)
                    }

                    if (familyName != null) {
                        pref.writeStringToPref(Constants.PrefKey.PREF_LAST_NAME, familyName)
                    }

                    intent = Intent(this, HomeActivity::class.java)
                        .putExtra(
                            Constants.BundleKey.HOME_ACTIVITY_KEY,
                            Constants.BundleKey.HOME_CALL_FROM_LOGIN
                        )

                    startActivity(intent)

//        val account = GoogleSignIn.getLastSignedInAccount(this)


                } catch (e: ApiException) {
                    // The ApiException status code indicates the detailed failure reason.
                    // Please refer to the GoogleSignInStatusCodes class reference for more information.
                    Log.w("Clinicapp", "signInResult:failed code=$e")
                }

            }

    }
}