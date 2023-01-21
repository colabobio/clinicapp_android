/**
 *  Part of ClinicApp
 *
 */

package org.broadinstitute.clinicapp.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils
import org.broadinstitute.clinicapp.util.SharedPreferencesOAuth2Storage

/**
 * The base activity of the app
 *
 * ...
 */
abstract class BaseActivity : AppCompatActivity(), MvpView {

    private var mProgressDialog: Dialog? = null
    lateinit var storage :SharedPreferencesOAuth2Storage
    lateinit var pref :SharedPreferenceUtils
    lateinit var userId :String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customActionBarLogo()

        //Creates an instance of the SharedPreferencesOAuth2Storage using the ClinicApp class
        storage  = ClinicApp.instance!!.getStorage()

        //Creates an instance of the SharedPreferencesUtils using the ClinicApp class
        pref  =  ClinicApp.instance!!.getPrefStorage()

        //get's the userId from SharedPreferencesUtils
        userId = pref.readStringFromPref(Constants.PrefKey.PREF_USER_NAME).toString()

    }


    /**
     * Shows the loading screen.
     */
    override fun showLoading() {
        hideLoading()
        mProgressDialog = CommonUtils.showLoadingDialog(this)
        mProgressDialog!!.show()
    }

    override fun hideLoading() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun customActionBarLogo() {

        supportActionBar?.setDisplayShowTitleEnabled(false)
        val logoView = ImageView(applicationContext)
        val layoutparams = RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
        logoView.layoutParams = layoutparams
        logoView.setImageResource(R.mipmap.logo)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.customView = logoView

    }

    private fun showSnackBar(message: String) {
        val snack = Snackbar.make(
            findViewById(android.R.id.content),
            message, Snackbar.LENGTH_SHORT
        )
        val sbView = snack.view
        val textView = sbView
            .findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
        if(message.isNotEmpty()) {
            snack.show()
        }
    }

    override fun onError(message: String?) {
        if (message != null) {
            showSnackBar(message)
        }
    }

    override fun onError(@StringRes resId: Int) {
        onError(getString(resId))
    }

    override fun showMessage(message: String?) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }


    override fun isNetworkConnected(): Boolean {
        return NetworkUtils.isNetworkConnected(applicationContext)
    }

     fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }



    protected abstract fun setUp()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun handleAuthError(){

            CommonUtils.showDialog(
                this, getString(R.string.error) , getString(R.string.auth_error),
                getString(R.string.ok), getString(R.string.cancel),
                object : CommonUtils.DialogCallback {
                    override fun positiveClick() {
                        val intent = Intent(this@BaseActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                    override fun negativeClick() {

                    }
                })
        }


}