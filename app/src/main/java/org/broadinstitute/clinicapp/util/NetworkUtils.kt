package org.broadinstitute.clinicapp.util

import android.content.Context
import android.net.ConnectivityManager

/**
 *  Class check the network connectivity
 */
object NetworkUtils {

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
}