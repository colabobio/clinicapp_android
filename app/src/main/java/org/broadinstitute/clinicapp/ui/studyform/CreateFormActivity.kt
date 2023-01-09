package org.broadinstitute.clinicapp.ui.studyform

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.BundleKey.CREATE_STUDY_FORM_KEY
import org.broadinstitute.clinicapp.Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
import org.broadinstitute.clinicapp.Constants.CallingPageValue.IMPORT_FROM_ONLINE_STUDY_FORM
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studyform.details.DetailsActivity
import org.broadinstitute.clinicapp.ui.studyform.info.InfoFragment

class CreateFormActivity : BaseActivity(),
    ItemFragment.OnListFragmentInteractionListener {

    private var isSearchEnable = false
    private var callingPage = CREATE_STUDY_FORM_KEY

    //creates the basic fragment that's shown when started
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_create_form)
        // Add fragment here... First step on create
        setUp()
    }

    override fun setUp() {

        val transaction = this.supportFragmentManager.beginTransaction()

        if (intent.hasExtra(CREATE_STUDY_FORM_KEY)) {
            //Checks to see if the intent has the key "CREATE_STUDY_FORM_KEY" then calls the corresponding value in the key value pair
            when (val key = intent.getStringExtra(CREATE_STUDY_FORM_KEY)) {

                //When the value for the key is "CREATE_FROM_SCRATCH_STUDY_FORM"
                CREATE_FROM_SCRATCH_STUDY_FORM -> {
                    var studyFormDetail: StudyFormDetail? = null
                    if (intent.hasExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY)) {
                        studyFormDetail =
                            intent.getParcelableExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY)
                    }
                    isSearchEnable = false
                    transaction.add(
                        R.id.fragment_parentLayout,
                        InfoFragment.newInstance(studyFormDetail, key)
                    )
                    callingPage = key
                }

                //When the value for the key is others
                else -> {
                    isSearchEnable = true
                    transaction.add(R.id.fragment_parentLayout, ItemFragment.newInstance(key.toString()))

                    if (key != null) {
                        callingPage = key
                    }
                }
            }

        }
        transaction.commit()
    }


    override fun onListFragmentInteraction(item: StudyFormDetail) {

        val masterStudyForm = item.masterStudyForms
        masterStudyForm.studyFormVariables = item.variables
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra(
            Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY,
            callingPage
        )
        intent.putExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, item)
        // Handle navigation when import button click it will redirect to home screen.
        if(callingPage == IMPORT_FROM_ONLINE_STUDY_FORM)
            startActivityForResult(intent,  ItemFragment.REQUEST_DETAILS_CODE)
        else startActivity(intent)

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
