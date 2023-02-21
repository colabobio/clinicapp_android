package org.broadinstitute.clinicapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.HomeActivity

class LoginActivity : BaseActivity() {
    private lateinit var signInClient: GoogleSignInClient
    private val RC_SIGN_IN = 612  // Can be any integer unique to the Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setUp()
    }

    override fun onStart() {
        super.onStart()

        var button = findViewById<SignInButton>(R.id.sign_in_button)
        button.setOnClickListener {
            signIn()
        }
    }

    override fun setUp() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(org.broadinstitute.clinicapp.BuildConfig.web_client_id)
            .requestScopes(Scope(Scopes.DRIVE_FULL))
            .requestEmail()
            .build()

        signInClient = GoogleSignIn.getClient(this, gso);
    }

    fun signIn() {
        // Signing out just in case the user is returning from the home screen
        signInClient.signOut()

        val signInIntent: Intent = signInClient.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode === RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val id = account.id
            val email = account.email
            val firstName = account.givenName
            val familyName = account.familyName

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
                .putExtra(Constants.BundleKey.HOME_ACTIVITY_KEY, Constants.BundleKey.HOME_CALL_FROM_LOGIN)
            startActivity(intent)

//        val account = GoogleSignIn.getLastSignedInAccount(this)


        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Clinicapp", "signInResult:failed code=" + e.getStatusCode())
        }
    }
}