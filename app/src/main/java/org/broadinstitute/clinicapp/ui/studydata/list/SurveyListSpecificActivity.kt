package org.broadinstitute.clinicapp.ui.studydata.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.Single
import kotlinx.android.synthetic.main.activity_specific_survey_list.*
import kotlinx.android.synthetic.main.activity_survey_list.*
import kotlinx.android.synthetic.main.activity_survey_list.emptyView
import kotlinx.android.synthetic.main.activity_survey_list.progressBar
import kotlinx.android.synthetic.main.activity_survey_list.txtSurveyListHeader
import kotlinx.android.synthetic.main.pop_create_specific_studydata.view.*
import kotlinx.android.synthetic.main.pop_create_studydata.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.patient.SearchPatientActivity
import org.broadinstitute.clinicapp.ui.studydata.survey.SurveyActivity


class SurveyListSpecificActivity : BaseActivity(), SDListContract.View {

    private lateinit var listAdapter: StudyDataSpecificAdapter
    private lateinit var searchView: SearchView
    private lateinit var studyFormDetail: StudyFormDetail
    private lateinit var masterStudyData: MasterStudyData
    private lateinit var presenter: SurveyListPresenter
    private lateinit var patient: Patient
    private var type: Int = 0

    override fun setUp() {
        studyFormDetail =
            intent.extras?.get(Constants.BundleKey.STUDY_FORM_DETAIL_KEY) as StudyFormDetail
        masterStudyData =
            intent.extras?.get(Constants.BundleKey.MASTER_STUDY_DATA_KEY) as MasterStudyData
        type = intent.getIntExtra(Constants.BundleKey.CREATE_STUDY_DATA_KEY, 0)

        presenter.getPatients(masterStudyData.adminId, studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!)

        txtSurveyListHeader.text =
            getString(R.string.patient_survey_list_title, masterStudyData.adminId, studyFormDetail.masterStudyForms.title)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_specific_survey_list)
        Log.d("SurveyListSpecificActivity onCreate", "surveylist")
        presenter = SurveyListPresenter(this, this, pref)
        setUp()

        val linearLayoutManager = LinearLayoutManager(this)
        rvStudyDataSpecific.layoutManager = linearLayoutManager
        listAdapter = StudyDataSpecificAdapter(studyFormDetail)
        rvStudyDataSpecific.adapter = listAdapter

        fabPatient.setOnClickListener {
            showCreationStudyDataDialog()
        }
    }

    @SuppressLint("InflateParams")
    private fun showCreationStudyDataDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pop_create_specific_studydata, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val mAlertDialog = mBuilder.show()
//        mDialogView.pop_create_new.setOnClickListener {
//            mAlertDialog.dismiss()
//            intent = Intent(this, SurveyActivity::class.java)
//                .putExtra(
//                    Constants.BundleKey.CREATE_STUDY_DATA_KEY,
//                    Constants.StudyDataType.NEW_PATIENT_STUDY_DATA
//                )
//                .putExtra(
//                    Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
//                    studyFormDetail
//                )
//            startActivity(intent)
//        }

        mDialogView.pop_create_followup.setOnClickListener {
            Log.d("SurveyListSpecificActivity follow_up Called", "surveylist")
            Log.d("SurveyListSpecificActivity follow_up Called", patient.toString())
            mAlertDialog.dismiss()
//            intent = Intent(this, SearchPatientActivity::class.java)
            Log.d("SurveyListSpecificActivity after dialog dismiss Called", patient.toString())
            if (patient != null){
                Log.d("follow_up Called", patient.toString())
                intent = Intent(this, SurveyActivity::class.java)
                    .putExtra(
                        Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                        Constants.StudyDataType.FOLLOWUP_STUDY_DATA //type = 1
                    )
                    .putExtra("CAN_APPLY_MODEL", "no_model")
//                    .putExtra(Constants.BundleKey.PATIENT_KEY, patient)
//                    .putExtra(Constants.BundleKey.CREATE_STUDY_DATA_KEY, type)
                    .putExtra(
                        Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                        studyFormDetail
                    )
                    .putExtra(Constants.BundleKey.PATIENT_KEY,
                        patient
                    )
                startActivity(intent)
            }
        }

        mDialogView.pop_create_final_outcome.setOnClickListener {
            mAlertDialog.dismiss()
//            intent = Intent(this, SearchPatientActivity::class.java)
            Log.d("final_outcome Called", patient.toString())
            if (patient != null){
                intent = Intent(this, SurveyActivity::class.java)
                    .putExtra(
                        Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                        Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA //type = 2
                    )
                    .putExtra("CAN_APPLY_MODEL", "no_model")
                    .putExtra(
                        Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                        studyFormDetail
                    )
                    .putExtra(Constants.BundleKey.PATIENT_KEY,
                        patient
                    )
//                startForResult.launch(intent)
                startActivity(intent)
            }


        }

    }

//    // Define Activity Result contract
//    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == RESULT_OK) {
//            // Handle the result if needed
//        }
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.menu_search_form, menu)
//        val searchItem = menu!!.findItem(R.id.action_search)
//        searchView = searchItem.actionView as SearchView
//        searchView.isSubmitButtonEnabled = true
//        searchView.queryHint = getString(R.string.search_study_data)
//
//        val searchSubmit =
//            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
//        searchSubmit.setImageResource(R.mipmap.ic_search)
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String): Boolean {
//                if (newText.isEmpty()) {
//                    hideKeyboard()
//                    presenter.getStudyDataFromDB(
//                        studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!, ""
//                    )
//                }
//                return true
//            }
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//               // hideKeyboard(searchView)
//                presenter.searchStudyDataFromDB(
//                    query.trim(),
//                    studyFormDetail.masterStudyForms.tempMasterStudyFormsId.toString()
//                )
//                return true
//            }
//        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else hideLoading()
    }

    override fun showSnackBarMessage(message: String) {

    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }



    override fun showProgressBar(isShow: Boolean) {
        if(isShow) progressBar.visibility = View.VISIBLE
            else progressBar.visibility = View.GONE
    }

    override fun showEmptyWarning(isEmpty: Boolean) {
        if(isEmpty) emptyView.visibility = View.VISIBLE
        else emptyView.visibility = View.GONE
    }

    override fun showPatients(patientList: PagedList<Patient>) {
        patient = patientList.get(0)!!
    }

    override fun initializePatients(patients: Patient) {
        patient =  patients
        Log.d("These are the initialized patients", patient.toString())
    }

    override fun showStudyData(list: PagedList<MasterStudyData>) {
       listAdapter.submitList(list)
    }


    override fun onResume() {
        super.onResume()
        if (listAdapter.currentList.isNullOrEmpty()) {
            presenter.checkStudyDataSpecificInDB(studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!,masterStudyData.adminId,
                studyFormDetail.masterStudyForms.isServerUpdated)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

}
