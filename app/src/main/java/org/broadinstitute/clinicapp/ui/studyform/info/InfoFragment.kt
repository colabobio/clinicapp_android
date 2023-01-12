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
        //Checks if the added document is a duplicate or not
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
                    //if it is not duplicate, call a new instance of the VCFragment (list of variables)
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

    //Gets the values assigned from companion object i.e "ARG_STUDY_FORM_DETAILS" and "ARG_CALLING_PAGE" to "studyFormDetail" and "callingPage" respectively
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        presenter = InfoPresenter(this, requireContext())

        //adds values to
        arguments?.let {
            studyFormDetail = it.getParcelable(ARG_STUDY_FORM_DETAILS)
            callingPage = it.getString(ARG_CALLING_PAGE).toString()
        }
    }

    /**
     * This is the onCreateView() function that's responsible for creating a view corresponding to
     * user input into the title and description fields of creating a study form.
     *
     * @Return a view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info, container, false)
        //checks to make sure that the studyFormDetail isn't null
        if (studyFormDetail != null) {
            view.Info_titleTxt.setText(studyFormDetail!!.masterStudyForms.title)
            view.Info_titleDesc.setText(studyFormDetail!!.masterStudyForms.description)
        }

        //Called after the Continue button is clicked to create the study form from scratch
        view.btn_continue.setOnClickListener {
                context?.let { it1 -> hideKeyboard(it1, view) }
            //checks to make sure that the bodies/fields of the study title and description aren't empty
            if (view.Info_titleTxt.text.toString().trim().isEmpty() || view.Info_titleDesc.text.toString().trim().isEmpty()) {
                //if so, throw an error
                Snackbar.make(
                    it,
                    getString(R.string.error_no_title_description),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                //check to make sure that a duplicate title doesn't already exist
                //if a duplicate title exists, isDuplicateTitle() will throw an error
                presenter.isDuplicateTitle(view.Info_titleTxt.text.toString())
            }
        }

        //returns correct, corresponding view
        return view
    }


    //Creates a new instance by adding passed values from "CreateFormActivity" class to ARG_STUDY_FORM_DETAILS and ARG_CALLING_PAGE
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
