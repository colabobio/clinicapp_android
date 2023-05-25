package org.broadinstitute.clinicapp.ui.studydata.survey

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail


class SurveyActivity : BaseActivity() {

    private val vm: SharedViewModel by lazy {
        ViewModelProviders.of(this).get(SharedViewModel::class.java)
    }
    private var form: StudyFormDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_form_details)
        setUp()
    }

    override fun setUp() {

        form = intent.getParcelableExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY)
        val type: Int = intent.getIntExtra(Constants.BundleKey.CREATE_STUDY_DATA_KEY,0)
        val masterID: MasterStudyData? = intent.getParcelableExtra(Constants.BundleKey.MASTER_STUDY_DATA_KEY)
        val patient: Patient? = intent.getParcelableExtra(Constants.BundleKey.PATIENT_KEY)
        Log.d("patientSurveyActivity", patient.toString())

        form?.let { it1 -> vm.select(it1) }
        // set value to screen type object and get master variable by providing study form.
        type.let {
            vm.setType(it, pref)
            println("TYPE is: $it")
        }
        // Update or edit study data case it value present otherwise its null
        // set value to Master study data object and get master variable by providing list of temp variable
        masterID?.let{it -> vm.setMasterStudyDataID(it)}

        // Follow up & final outcome flow patient value is present, we need create master study data
        patient?.let{it -> vm.setPatient(it)}

        if(type == Constants.StudyDataType.UPDATE_STUDY_DATA){
            masterID?.adminId?.let { vm.getPatient(it) }
        }

        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.flAddMoreVars,
            AboutFragment.newInstance(masterID))
        transaction.commit()

    }

    override fun onDestroy() {
        super.onDestroy()
        vm.clearDisposable()

    }


    override fun onBackPressed() {
        super.onBackPressed()
        if(this.supportFragmentManager.popBackStackImmediate("complete", 0)){
            finish()
        }
    }
}
