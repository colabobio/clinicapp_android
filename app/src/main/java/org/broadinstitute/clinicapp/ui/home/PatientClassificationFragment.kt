package org.broadinstitute.clinicapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail

class PatientClassificationFragment : Fragment() {
    private var studyForMDetail: StudyFormDetail? = null
    private var variablesAndValue: LinkedHashMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            studyForMDetail = it.getParcelable("studyForm") as StudyFormDetail?
            variablesAndValue = it.getSerializable("variablesAndValue") as? LinkedHashMap<String, String>
        }
        println("studyForMDetail in Models = $studyForMDetail")
        println("variablesAndValue in Models = $variablesAndValue")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_patient_classification, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PatientClassificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(studyForMDetail: StudyFormDetail?, variablesAndValue: LinkedHashMap<String, String>) =
            PatientClassificationFragment().apply {
                println("variablesAndValue in newInstance For Models = $variablesAndValue")
                arguments = Bundle().apply {
                    putParcelable("studyForm", studyForMDetail)
                    putSerializable("variableAndValues", variablesAndValue)
                }
            }
    }
}