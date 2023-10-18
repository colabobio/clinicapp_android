package org.broadinstitute.clinicapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.home.HomeActivity
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.broadinstitute.clinicapp.util.TokenHelper


class SplashActivity : BaseActivity() {

    override fun setUp() {

    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUp()


        Handler(Looper.getMainLooper()).postDelayed({
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)

    }

   }
