package org.broadinstitute.clinicapp.ui.studydata.patient

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_patient_list.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail


class SearchPatientActivity : BaseActivity(), PatientListContract.View {
    private lateinit var searchView: SearchView
    private lateinit var studyFormDetail: StudyFormDetail
    private lateinit var presenter: PatientListPresenter
    private lateinit var listAdapter: PatientAdapter
    private var type: Int = 0

    override fun setUp() {
        studyFormDetail = intent.getParcelableExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY)
        type = intent.getIntExtra(Constants.BundleKey.CREATE_STUDY_DATA_KEY, 0)

        presenter.getPatients("", studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_patient_list)

        presenter = PatientListPresenter(this, this)
        setUp()

        val linearLayoutManager = LinearLayoutManager(this)
        rvPatient.layoutManager = linearLayoutManager
        listAdapter = PatientAdapter(this, studyFormDetail, type)
        rvPatient.adapter = listAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SEARCH_PATIENT_CODE) {
            if (resultCode == Activity.RESULT_OK)
                setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun showEmptyWarning(isEmpty: Boolean) {
        if(isEmpty) emptyView.visibility = View.VISIBLE
        else emptyView.visibility = View.GONE
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search_form, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.search_patient)

        val searchSubmit =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchSubmit.setImageResource(R.mipmap.ic_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    presenter.getPatients(
                        "",
                        studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!
                    )
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                presenter.getPatients(
                    query.trim(),
                    studyFormDetail.masterStudyForms.tempMasterStudyFormsId!!
                )
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun showPatients(patientList: PagedList<Patient>) {
        listAdapter.submitList(patientList)
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

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    companion object {
        const val REQUEST_SEARCH_PATIENT_CODE = 200
    }

}
