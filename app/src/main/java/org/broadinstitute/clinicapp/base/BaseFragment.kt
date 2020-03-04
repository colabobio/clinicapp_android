package org.broadinstitute.clinicapp.base

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import org.broadinstitute.clinicapp.R
import androidx.fragment.app.Fragment
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.ui.login.LoginActivity

import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils


abstract class BaseFragment : Fragment(), MvpView {
    private var mProgressDialog: Dialog? = null
    protected lateinit var pref :SharedPreferenceUtils
    lateinit var userID : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref  =  ClinicApp.instance!!.getPrefStorage()
        userID = pref.readStringFromPref(Constants.PrefKey.PREF_USER_NAME).toString()

    }


    override fun showLoading() {
       // Log.v("BaseActivity", "showLoading")
        hideLoading()
        mProgressDialog = CommonUtils.showLoadingDialog(requireContext())
        mProgressDialog!!.show()
    }

    override fun hideLoading() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.cancel()
        }
    }


    private fun showSnackBar(message: String) {
        val snack = Snackbar.make(
            activity?.findViewById(android.R.id.content)!!,
            message, Snackbar.LENGTH_SHORT
        )
        val sbView = snack.view
        val textView = sbView
            .findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        snack.show()
    }

    override fun onError(message: String?) {
        if (message != null) {
            showSnackBar(message)
        } else {
            showSnackBar(getString(R.string.snack_bar_text))
        }
    }

    override fun onError(@StringRes resId: Int) {
        onError(getString(resId))
    }

    override fun showMessage(message: String?) {
        if (message != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun isNetworkConnected(): Boolean {
        return NetworkUtils.isNetworkConnected(requireContext())
    }

    fun hideKeyboard(context: Context, view: View) {

        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun handleAuthError(){
        activity?.let {
            CommonUtils.showDialog(
                it, getString(R.string.error) , getString(R.string.auth_error),
                getString(R.string.ok), getString(R.string.cancel),
                object : CommonUtils.DialogCallback {
                    override fun positiveClick() {
                        val intent = Intent(activity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity?.finish()
                    }

                    override fun negativeClick() {

                    }
                })
        }
    }


}