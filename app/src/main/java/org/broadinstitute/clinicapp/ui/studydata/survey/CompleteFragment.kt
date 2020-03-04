package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.survey_complete.view.*
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment


class CompleteFragment : BaseFragment(){

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.survey_complete, container, false)

        view.complete_txtHeader.text = getString(R.string.complete_survey)
        view.complete_surveyOkBtn.visibility = View.VISIBLE

        view.complete_surveyOkBtn.setOnClickListener {
            activity?.setResult(Activity.RESULT_OK)
            activity?.finish()
        }

        return view
    }


    companion object {

        @JvmStatic
        fun newInstance() =
            CompleteFragment()
    }

}
