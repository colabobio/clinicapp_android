package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_about_study_form.view.*
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail


class AboutFragment : BaseFragment(){


    private val model: SharedViewModel by activityViewModels()
    private var studyFormDetail: StudyFormDetail? = null
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studyFormDetail = model.selected.value

    }

    @SuppressLint("StringFormatInvalid", "CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_about_study_form, container, false)

        view.txtHeader.text = getString(R.string.about_study_data)
        view.btn_confirm.visibility = View.VISIBLE
        progressBar = view.aboutProgressBar
        view.btn_confirm.text= getString(R.string.continue_str)
        if(studyFormDetail!=null){
            view.about_study_form_details_title.text = studyFormDetail?.masterStudyForms?.title
            view.study_form_details_desc.text = studyFormDetail?.masterStudyForms?.description
        }

        view.btn_confirm.setOnClickListener {
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.add(R.id.flAddMoreVars, FillStudiesFragment.newInstance(),"Fill_data")
            transaction?.addToBackStack("about")
            transaction?.commit()
        }

        model.source
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
            if (it == true) {
                view.btn_confirm.isEnabled = false
                progressBar.visibility = View.VISIBLE
            } else {
                view.btn_confirm.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
        return view
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance() =
            AboutFragment().apply {

            }

    }

}
