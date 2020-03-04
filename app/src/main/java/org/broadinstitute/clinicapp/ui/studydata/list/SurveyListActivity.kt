package org.broadinstitute.clinicapp.ui.studydata.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_survey_list.*
import kotlinx.android.synthetic.main.pop_create_studydata.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.patient.SearchPatientActivity
import org.broadinstitute.clinicapp.ui.studydata.survey.SurveyActivity


class SurveyListActivity : BaseActivity(), SDListContract.View {

    private lateinit var listAdapter: StudyDataAdapter
    private lateinit var searchView: SearchView

    private lateinit var studyFormDetail: StudyFormDetail
    private lateinit var presenter: SurveyListPresenter

    override fun setUp() {
        studyFormDetail =
            intent.extras?.get(Constants.BundleKey.STUDY_FORM_DETAIL_KEY) as StudyFormDetail

        txtSurveyListHeader.text =
            getString(R.string.survey_list_title, studyFormDetail.masterStudyForms.title)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_survey_list)
        presenter = SurveyListPresenter(this, this, pref)
        setUp()

        val linearLayoutManager = LinearLayoutManager(this)
        rvStudyData.layoutManager = linearLayoutManager
        listAdapter = StudyDataAdapter(studyFormDetail)
        rvStudyData.adapter = listAdapter

        fab.setOnClickListener {
            showCreationStudyDataDialog()
        }
    }

    @SuppressLint("InflateParams")
    private fun showCreationStudyDataDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pop_create_studydata, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        val mAlertDialog = mBuilder.show()
        mDialogView.pop_create_new.setOnClickListener {
            mAlertDialog.dismiss()
            intent = Intent(this, SurveyActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                    Constants.StudyDataType.NEW_PATIENT_STUDY_DATA
                )
                .putExtra(
                    Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                    studyFormDetail
                )
            startActivity(intent)
        }

        mDialogView.pop_create_followup.setOnClickListener {
            mAlertDialog.dismiss()
            intent = Intent(this, SearchPatientActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                    Constants.StudyDataType.FOLLOWUP_STUDY_DATA
                )
                .putExtra(
                    Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                    studyFormDetail
                )
            startActivity(intent)
        }

        mDialogView.pop_create_final_outcome.setOnClickListener {
            mAlertDialog.dismiss()
            intent = Intent(this, SearchPatientActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                    Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA
                )
                .putExtra(
                    Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                    studyFormDetail
                )
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search_form, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.search_study_data)

        val searchSubmit =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchSubmit.setImageResource(R.mipmap.ic_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    hideKeyboard()
                    presenter.getStudyDataFromDB(
                        studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!, ""
                    )
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
               // hideKeyboard(searchView)
                presenter.searchStudyDataFromDB(
                    query.trim(),
                    studyFormDetail.masterStudyForms.tempMasterStudyFormsId.toString()
                )
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else hideLoading()
    }

    override fun showSnackBarMessage(message: String) {
        Log.d("tag", message)
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

    override fun showStudyData(list: PagedList<MasterStudyData>) {
       listAdapter.submitList(list)
    }


    override fun onResume() {
        super.onResume()
        if (listAdapter.currentList.isNullOrEmpty()) {
            presenter.checkStudyDataInDB(studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!,
                studyFormDetail.masterStudyForms.isServerUpdated)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

}
