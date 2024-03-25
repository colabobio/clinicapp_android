package org.broadinstitute.clinicapp.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.pop_create_studyform.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.OnSyncInteractionListener
import org.broadinstitute.clinicapp.ui.studyform.CreateFormActivity
import org.broadinstitute.clinicapp.ui.studyform.ItemFragment
import org.broadinstitute.clinicapp.ui.studyform.variableselection.AddVariableActivity
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils

class FragmentMyStudies : Fragment(), HomeContract.View, OnSyncInteractionListener, CoroutineScope by MainScope() {
    private lateinit var presenter: HomePresenter
    lateinit var storage: SharedPreferences
    lateinit var pref: SharedPreferenceUtils
    lateinit var userId: String
    private var mProgressDialog: Dialog? = null
    private lateinit var listAdapter: StudyFormsAdapter
    private lateinit var rvStudyForms: RecyclerView
    private lateinit var intent: Intent
    private lateinit var fab: FloatingActionButton
    private var isSearchResultLoaded = false


    fun setUp() {
        if(pref.readBooleanFromPref(Constants.PrefKey.PREF_INITIAL_LAUNCH, true))
            presenter.checkMasterVariables(0)
        else presenter.checkStudyFormsInDB(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage  = ClinicApp.instance!!.getStorage()
        pref  =  ClinicApp.instance!!.getPrefStorage()
        userId = pref.readStringFromPref(Constants.PrefKey.PREF_USER_NAME).toString()
        presenter = HomePresenter(this, requireContext().applicationContext, pref)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.content_home, container, false)

        setUp()

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            showCreationFormDialog()
        }
        rvStudyForms = view.findViewById(R.id.rvStudyForms)

        val linearLayoutManager = LinearLayoutManager(context)
        rvStudyForms.layoutManager = linearLayoutManager
        listAdapter = StudyFormsAdapter(userId, this)
        rvStudyForms.adapter = listAdapter
        presenter.getStudyFormsFromDB("")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_search_form, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId){
                    R.id.action_search -> {
                        getStudies(menuItem)
//                        return true
                    }
                }
                return false
            }
        })


    }

    private fun getStudies(menuItem: MenuItem){
        val searchItem = menuItem.actionView as SearchView
        searchItem.isSubmitButtonEnabled = true
        val searchSubmit =
            searchItem.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchSubmit.setImageResource(R.mipmap.ic_search)
        searchItem.queryHint = getString(R.string.search_study_forms)
        searchItem.setOnSearchClickListener {
            searchItem.queryHint = getString(R.string.search_study_forms)
            searchItem.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isEmpty()) {
                        presenter.getStudyFormsFromDB("")
                    }
                    return true
                }
                override fun onQueryTextSubmit(query: String): Boolean {
                    isSearchResultLoaded = true
                    presenter.getStudyFormsFromDB(query.trim())
                    return true
                }
            })
        }
        searchItem.setOnCloseListener { !isSearchResultLoaded }
    }

    override fun showStudyForms(list: PagedList<StudyFormDetail>) {
        listAdapter.submitList(list)
        if(listAdapter.itemCount == 0){
            showEmptyWarning(true)
        }else{
            showEmptyWarning(false)
        }
    }

    override fun updateProgress(show: Boolean, syncMessage: String) {
        TODO("Not yet implemented")
    }

    override fun handleLogout(message: String, positiveOption: String, negativeOption: String) {
        TODO("Not yet implemented")
    }

    override fun showEmptyWarning(isEmpty: Boolean) {
//        if(isEmpty) form_emptyView.visibility = View.VISIBLE
//        else form_emptyView.visibility = View.GONE
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    override fun showSnackBarMessage(message: String) {
        TODO("Not yet implemented")
    }

    override fun showToastMessage(message: String) {
        TODO("Not yet implemented")
    }
    /**
     * Shows the loading screen.
     */
     fun showLoading() {
        hideLoading()
        mProgressDialog = CommonUtils.showLoadingDialog(requireContext().applicationContext)
        mProgressDialog!!.show()
    }

    fun hideLoading() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onSyncClick(item: StudyFormDetail) {
        if(isNetworkConnected())
            presenter.syncIndividualForm(item)
        else showSnackBarMessage(getString(R.string.network_error))
    }

    fun isNetworkConnected(): Boolean {
        return NetworkUtils.isNetworkConnected(requireContext().applicationContext)
    }

    @SuppressLint("InflateParams")
    private fun showCreationFormDialog() {

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.pop_create_studyform, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.pop_create_scratch.setOnClickListener {
            mAlertDialog.dismiss()
            intent = Intent(context, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
                )

            startActivity(intent)
        }
        //cancel button click of custom layout
        mDialogView.pop_create_existing.setOnClickListener {
            intent = Intent(context, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM
                )

            startActivity(intent)
            mAlertDialog.dismiss()
        }

        if (isNetworkConnected()) {
            mDialogView.pop_import_online.isEnabled = true
            mDialogView.pop_import_online.setBackgroundResource(R.drawable.button_bg)
        } else {
            mDialogView.pop_import_online.setBackgroundResource(R.drawable.inactive_button_bg)
            mDialogView.pop_import_online.isEnabled = false
        }
        mDialogView.pop_import_online.setOnClickListener {
            intent = Intent(context, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.IMPORT_FROM_ONLINE_STUDY_FORM
                )

            startActivity(intent)
            mAlertDialog.dismiss()
        }
        mDialogView.add_variable.setOnClickListener {
            intent = Intent(context, AddVariableActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.IMPORT_FROM_ONLINE_STUDY_FORM
                )

            startActivity(intent)
            mAlertDialog.dismiss()
        }

    }
}

