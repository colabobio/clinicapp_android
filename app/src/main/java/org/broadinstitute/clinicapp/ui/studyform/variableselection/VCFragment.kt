package org.broadinstitute.clinicapp.ui.studyform.variableselection

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_form_creation.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
import org.broadinstitute.clinicapp.Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM
import org.broadinstitute.clinicapp.Constants.CallingPageValue.HOME_EDIT_STUDY_FORM
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormVariables
import org.broadinstitute.clinicapp.util.CommonUtils


class VCFragment : BaseFragment(), VCContract.View, OnInfoSelectedListener,
    OnVariableSelectedListener {
    private lateinit var listAdapter: VCExpandableAdapter
    private lateinit var presenter: VCPresenter
    private var categoryList: ArrayList<String> = ArrayList()
    private var mvMap: HashMap<String, List<MasterVariables>> = HashMap()

    private var selectedVariables: ArrayList<Long> = ArrayList()
    private var oldFormVariablesList: ArrayList<StudyFormVariables> = ArrayList()

    private var studyForm: MasterStudyForms? = null
    private var studyFormTitle: String = ""
    private var studyFormDescription: String = ""
    private var receivedStudyFormDetail: StudyFormDetail? = null
    private var isReadOnly = false
    private var callingPage = HOME_EDIT_STUDY_FORM


    override fun onVariableSelected(masterVariables: MasterVariables, isSelected: Boolean) {

        val id = masterVariables.id
        if (isSelected) {
            if (!selectedVariables.contains(id))
                selectedVariables.add(id)
        } else {
            if (selectedVariables.contains(id))
                selectedVariables.remove(id)
        }
    }

    private fun populateSelectedVariables(studyFormDetail: StudyFormDetail) {
        studyFormDetail.variables.forEach { studyFormVariables ->
            if (studyFormVariables.isActive) {
                selectedVariables.add(studyFormVariables.masterVariablesIdFk)
            }
        }

    }

    private fun createNewFormVariables(): List<StudyFormVariables> {

        val formVariables = arrayListOf<StudyFormVariables>()

        oldFormVariablesList.forEach {
            if (!selectedVariables.contains(it.masterVariablesIdFk)) {
                if (it.isActive) {
                    it.isActive = false
                    it.lastModified = System.currentTimeMillis()
                    it.isServerUpdated = false
                }
                formVariables.add(it)
            } else {
                if (!it.isActive) {
                    it.isActive = true
                    it.lastModified = System.currentTimeMillis()
                    it.isServerUpdated = false
                }
                formVariables.add(it)
                // remove from list - duplicate variable will not created
                selectedVariables.remove(it.masterVariablesIdFk)
            }
        }

        var counter = 1
        selectedVariables.forEach { masterVariableID ->

            val currentTimeInMillis = System.currentTimeMillis()
            val variable = StudyFormVariables()
            variable.masterVariablesIdFk = masterVariableID
            variable.isActive = true
            variable.createdOn = currentTimeInMillis
            variable.lastModified = currentTimeInMillis
            variable.isServerUpdated = false
            variable.tempStudyFormVariablesId =
                CommonUtils.generateTempId(
                    Constants.TempIdType.STUDY_FORM_VARIABLE_TEMP,
                    userID, counter
                )
            variable.timezone = CommonUtils.getTimezone()
            counter++
            formVariables.add(variable)

        }
        return formVariables

    }

    override fun onInfoSelected(masterVariables: MasterVariables) {
        CommonUtils.onInfoSelected(masterVariables, requireContext())
    }

    override fun showMasterVariables(
        mvMap: HashMap<String, List<MasterVariables>>,
        catList: ArrayList<String>
    ) {
        this.mvMap = mvMap
        this.categoryList = catList
        this.categoryList.sort()
        listAdapter.setLists(this.mvMap, this.categoryList, this.selectedVariables)
        if (catList.isNotEmpty()) {
            view?.elvMasterVariablesMain?.expandGroup(0)
        }
    }

    private fun createStudyForm(title: String, description: String) {
        val tempId = CommonUtils.generateTempId(Constants.TempIdType.MASTER_STUDY_FORM_TEMP, userID)
        val currentTime = System.currentTimeMillis()
        studyForm = MasterStudyForms(
            title = title,
            description = description,
            tempMasterStudyFormsId = tempId,
            isActive = true,
            isServerUpdated = false,
            userId = userID,
            creator = pref.readStringFromPref(
                Constants.PrefKey.PREF_FIRST_NAME
            )!! + " " + pref.readStringFromPref(
                Constants.PrefKey.PREF_LAST_NAME
            )!!,
            createdOn = currentTime,
            lastModified = currentTime,
            version = currentTime.toString(),
            timezone = CommonUtils.getTimezone(),
            studyFormVariables = oldFormVariablesList // Not required to handle or it not be in use
        )
    }

    override fun showProgress(show: Boolean) {
        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()

    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_form_creation, container, false)

        //Adapter for all the variables
        listAdapter =
            VCExpandableAdapter(
                activity!!,
                isReadOnly,
                categoryList,
                selectedVariables,
                mvMap,
                this,
                this,
                callingPage
            )
        view.elvMasterVariablesMain.setAdapter(listAdapter)

        if (isReadOnly) {
            view.txtStudyFormCreationTitle.text = getString(R.string.view_variables)
            view.btnConfirm.visibility = View.GONE
        }

        //Used to confirm list of variables after selecting variables
        view.btnConfirm.setOnClickListener {

            studyForm?.let { it1 ->
                when {
                    selectedVariables.isNullOrEmpty() ->
                    showSnackBarMessage(getString(R.string.error_no_variable_selected))
                    callingPage == HOME_EDIT_STUDY_FORM -> {
                        it1.version = System.currentTimeMillis().toString()

                        it1.lastModified = System.currentTimeMillis()
                        it1.isServerUpdated = false
                        /**
                         * THE END OF CREATING A STUDY FORM
                         */
                        //inserts the variables in the database
                        presenter.insertMasterStudyForm(it1, createNewFormVariables())

                    }
                    callingPage == CREATE_FROM_TEMPLATE_STUDY_FORM ||
                            callingPage == CREATE_FROM_SCRATCH_STUDY_FORM -> presenter.insertMasterStudyForm(
                        it1,
                        createNewFormVariables()
                    )
                }
            } // end of let

        }

        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {
            studyFormTitle = it.getString(ARG_STUDY_FORM_TITLE).toString()
            studyFormDescription = it.getString(ARG_STUDY_FORM_DESCRIPTION).toString()
            receivedStudyFormDetail = it.getParcelable(ARG_STUDY_FORM_DETAIL)
            isReadOnly = it.getBoolean(ARG_IS_READ_ONLY)
            callingPage = it.getString(ARG_CALLING_PAGE).toString()
        }

        //START - Home Edit study form flow
        if (callingPage == HOME_EDIT_STUDY_FORM) {
            studyForm = receivedStudyFormDetail!!.masterStudyForms
            oldFormVariablesList =
                receivedStudyFormDetail!!.variables as ArrayList<StudyFormVariables>

            populateSelectedVariables(receivedStudyFormDetail!!)
        }
        //END

        //START - Create new from Scratch flow
        else if (callingPage == CREATE_FROM_SCRATCH_STUDY_FORM) {
            //START - Create new from Template flow
            if (receivedStudyFormDetail != null) {
                createStudyForm(
                    receivedStudyFormDetail!!.masterStudyForms.title,
                    receivedStudyFormDetail!!.masterStudyForms.description
                )
                populateSelectedVariables(receivedStudyFormDetail!!)
                // END
            } else {
                createStudyForm(studyFormTitle, studyFormDescription)
            }
        }
        //END

        //Show the variables for selection
        presenter = VCPresenter(this, requireContext())
        presenter.getCategories(20, 0)
    }

    companion object {
        private const val ARG_STUDY_FORM_TITLE = "ARG_STUDY_FORM_TITLE"
        private const val ARG_STUDY_FORM_DESCRIPTION = "ARG_STUDY_FORM_DESCRIPTION"
        private const val ARG_STUDY_FORM_DETAIL = "ARG_STUDY_FORM_DETAIL"
        private const val ARG_IS_READ_ONLY = "ARG_IS_READ_ONLY"
        private const val ARG_CALLING_PAGE = "ARG_CALLING_PAGE"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment VCFragment.
         */
        @JvmStatic
        fun newInstance(title: String, description: String, callingPage: String) =
            VCFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STUDY_FORM_TITLE, title)
                    putString(ARG_STUDY_FORM_DESCRIPTION, description)
                    putString(ARG_CALLING_PAGE, callingPage)
                }
            }

        @JvmStatic
        fun newInstance(studyForm: StudyFormDetail?, callingPage: String) =
            VCFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_STUDY_FORM_DETAIL, studyForm)
                    putString(ARG_CALLING_PAGE, callingPage)
                }
            }

        @JvmStatic
        fun newInstance(isReadOnly: Boolean) =
            VCFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_READ_ONLY, isReadOnly)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }
}