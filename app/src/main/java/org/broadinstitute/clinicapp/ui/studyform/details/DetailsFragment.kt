package org.broadinstitute.clinicapp.ui.studyform.details

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_form_details.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.CallingPageValue.HOME_EDIT_STUDY_FORM
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studyform.CreateFormActivity
import org.broadinstitute.clinicapp.ui.studyform.ItemFragment
import org.broadinstitute.clinicapp.ui.studyform.variableselection.VCFragment
import org.broadinstitute.clinicapp.util.CommonUtils


class DetailsFragment : BaseFragment(), DetailsContract.View {

    override fun showConfirmOption() {
        btnConfirm.visibility = View.VISIBLE
        btnAddMore.visibility = View.GONE
    }

    override fun showConfirmWithAdd() {
        btnConfirm.visibility = View.GONE
        btnAddMore.visibility = View.VISIBLE
    }

    private lateinit var listAdapter: DetailsVariableAdapter
    private lateinit var presenter: DetailsPresenter

    private val mValues = arrayListOf<StudyFormVariablesDao.StudyFormWithVariable>()

    private var studyFormDetail: StudyFormDetail? = null

    private lateinit var txtTitle: TextInputEditText
    private lateinit var txtDesc: TextInputEditText
    private lateinit var rvStudyFormsVariable: RecyclerView
    private lateinit var btnAddMore: Button
    private lateinit var btnConfirm: Button
    private lateinit var txtVariableLabel: AppCompatTextView
    private var callingPage = HOME_EDIT_STUDY_FORM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = DetailsPresenter(this, requireContext())
        arguments?.let {
            callingPage = it.getString(Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY).toString()
            studyFormDetail =
                it.getParcelable(Constants.BundleKey.STUDY_FORM_DETAIL_KEY)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_form_details, container, false).also {
            txtTitle = it.findViewById(R.id.study_form_details_title)
            txtDesc = it.findViewById(R.id.study_form_details_desc)
            rvStudyFormsVariable = it.findViewById(R.id.rvStudyFormsVariable)
            btnAddMore = it.findViewById(R.id.btn_AddMore)
            btnConfirm = it.findViewById(R.id.btn_confirm)
            txtVariableLabel = it.findViewById(R.id.studyFormDetails_variableLabel)
        }

        listAdapter = DetailsVariableAdapter(mValues)
        rvStudyFormsVariable.adapter = listAdapter

        if (studyFormDetail != null) {
            txtTitle.setText(studyFormDetail?.masterStudyForms?.title)
            txtDesc.setText(studyFormDetail?.masterStudyForms?.description)
            txtTitle.isEnabled = false
            txtDesc.isEnabled = false
            val colorDrawable = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.colorAppBackground))
            view.study_form_details_title_layout.background = colorDrawable
            view.study_form_details_desc_layout.background = colorDrawable

            when (callingPage) {
                HOME_EDIT_STUDY_FORM -> {
                    studyFormDetail?.masterStudyForms?.tempMasterStudyFormsId?.let { presenter.getMasterVariables(it) }
                    if (studyFormDetail!!.masterStudyForms.userId == userID) {
                        // Only read mode
                        showConfirmWithAdd()
                        txtTitle.isEnabled = true
                        txtDesc.isEnabled = true
                        view.study_form_details_title_layout.background = context?.getDrawable(R.drawable.card_background)
                        view.study_form_details_desc_layout.background = context?.getDrawable(R.drawable.card_background)

                    }
                }
                Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM -> {
                    presenter.getMasterVariables(studyFormDetail!!)
                    btnConfirm.text = getString(R.string.select_template)
                    showConfirmOption()
                }
                Constants.CallingPageValue.IMPORT_FROM_ONLINE_STUDY_FORM -> {
                    btnConfirm.text = getString(R.string.import_study_form)
                    showConfirmOption()
                    presenter.getMasterVariables(studyFormDetail!!)
                }
            }

        }

        btnConfirm.setOnClickListener {

            if (callingPage == Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM) {

                val intent = Intent(context, CreateFormActivity::class.java)
                intent.putExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, studyFormDetail)
                // We are trying to create new study form using template so pass CREATE_FROM_SCRATCH_STUDY_FORM value
                intent.putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
                )

                activity?.startActivityForResult(intent, ItemFragment.REQUEST_DETAILS_CODE)
            } else {
                studyFormDetail?.masterStudyForms?.let { it1 -> presenter.importStudyForm(userID, it1) }
            }

        }

        btnAddMore.setOnClickListener {

             if (studyFormDetail != null) {
                 val t = txtTitle.text.toString().trim()

                 if(t.isBlank()){
                     onError("Please enter title")
                 }else if(!studyFormDetail!!.masterStudyForms.title.equals(t, false)){
                     studyFormDetail!!.masterStudyForms.title = t
                     studyFormDetail!!.masterStudyForms.description = txtDesc.text.toString().trim()
                     presenter.isDuplicateTitle(t)
                 } else{
                     val transaction = this.activity?.supportFragmentManager?.beginTransaction()
                     transaction?.addToBackStack(null)
                     transaction?.add(
                         R.id.flAddMoreVars,
                         VCFragment.newInstance(
                             studyFormDetail!!, callingPage
                         )
                     )
                     transaction?.commit()
                 }
            }
        }

        return view
    }


    override fun isDuplicate(b: Boolean) {
        if(!b){
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.addToBackStack(null)
            transaction?.add(
                R.id.flAddMoreVars,
                VCFragment.newInstance(
                    studyFormDetail!!, callingPage
                )
            )
            transaction?.commit()
        } else {
            onError(R.string.error_duplicate_title)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance(studyFormDetail: StudyFormDetail, mCallingPage: String) =
            DetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, studyFormDetail)
                    putString(Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY, mCallingPage)
                }
            }

    }

    override fun showVariables(list: List<StudyFormVariablesDao.StudyFormWithVariable>) {
        if(list.isNullOrEmpty()){
            txtVariableLabel.text = context?.getString(R.string.warning_sync_master_variable)
        }else {
            txtVariableLabel.text = context?.getString(R.string.variables)
            listAdapter.notifyDataSetChanged()
            mValues.addAll(list)
        }

    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else hideLoading()
    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    override fun successImportStudy() {

        activity?.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    override fun handleThrowable(throwable: Throwable) {

        val errorMsg = CommonUtils.getErrorMessage(throwable)
        if(errorMsg == getString(R.string.unAuthorized_error)) {
            handleAuthError()
        }else{
            showSnackBarMessage(errorMsg)
        }
    }

}
