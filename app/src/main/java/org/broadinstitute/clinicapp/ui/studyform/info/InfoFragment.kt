package org.broadinstitute.clinicapp.ui.studyform.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_info.view.*
import org.broadinstitute.clinicapp.Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studyform.variableselection.VCFragment


private const val ARG_STUDY_FORM_DETAILS = "ARG_STUDY_FORM_DETAILS"
private const val ARG_CALLING_PAGE = "ARG_CALLING_PAGE"

class InfoFragment : BaseFragment(), InfoContract.View {
    override fun isDuplicate(isDuplicate: Boolean) {
        if (!isDuplicate) {
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.addToBackStack(null)

            if (studyFormDetail != null) {
                studyFormDetail!!.masterStudyForms.title =
                    view!!.Info_titleTxt.text.toString().trim()
                studyFormDetail!!.masterStudyForms.description =
                    view!!.Info_titleDesc.text.toString()
                transaction?.replace(
                    R.id.CreateFormLayout,
                    VCFragment.newInstance(studyFormDetail, callingPage)
                )
            } else {
                transaction?.replace(
                    R.id.CreateFormLayout,
                    VCFragment.newInstance(
                        view!!.Info_titleTxt.text.toString().trim(),
                        view!!.Info_titleDesc.text.toString(),
                        callingPage
                    )
                )
            }
            transaction?.commit()
        } else {
            onError(R.string.error_duplicate_title)
        }
    }

    override fun showProgress(show: Boolean) {

    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    private var studyFormDetail: StudyFormDetail? = null
    private var callingPage: String = CREATE_FROM_SCRATCH_STUDY_FORM
    private lateinit var presenter: InfoPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = InfoPresenter(this, requireContext())

        arguments?.let {
            studyFormDetail = it.getParcelable(ARG_STUDY_FORM_DETAILS)
            callingPage = it.getString(ARG_CALLING_PAGE).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        if (studyFormDetail != null) {
            view.Info_titleTxt.setText(studyFormDetail!!.masterStudyForms.title)
            view.Info_titleDesc.setText(studyFormDetail!!.masterStudyForms.description)
        }

        view.btn_continue.setOnClickListener {
                context?.let { it1 -> hideKeyboard(it1, view) }
            if (view.Info_titleTxt.text.toString().trim().isEmpty() || view.Info_titleDesc.text.toString().trim().isEmpty()) {
                Snackbar.make(
                    it,
                    getString(R.string.error_no_title_description),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                presenter.isDuplicateTitle(view.Info_titleTxt.text.toString())
            }
        }

        return view
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param studyFormDetail Parameter 1.
         * @param mCallingPage Parameter 2.
         * @return A new instance of fragment InfoFragment.
         */
        @JvmStatic
        fun newInstance(studyFormDetail: StudyFormDetail?, mCallingPage: String) =
            InfoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_STUDY_FORM_DETAILS, studyFormDetail)
                    putString(ARG_CALLING_PAGE, mCallingPage)

                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }
}
