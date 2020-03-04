package org.broadinstitute.clinicapp.ui.studyform.details

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.studyform.ItemFragment


class DetailsActivity : BaseActivity() {

    override fun setUp() {
        val transaction = this.supportFragmentManager.beginTransaction()

            transaction.add(
                R.id.flAddMoreVars,
                DetailsFragment.newInstance(
                    intent.getParcelableExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY),
                    intent.extras?.get(Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY).toString()
                )
            )

        transaction.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_form_details)
        setUp()
        Log.v("Calling Details", intent.extras?.get(Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY).toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ItemFragment.REQUEST_DETAILS_CODE){
            if(resultCode == Activity.RESULT_OK)
                setResult(Activity.RESULT_OK)
                finish()
        }
    }
}
