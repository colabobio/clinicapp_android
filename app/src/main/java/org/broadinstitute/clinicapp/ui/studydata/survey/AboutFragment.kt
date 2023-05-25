package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_about_study_form.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.email
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.fileFullNames
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.fileNames
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.filePaths
import org.broadinstitute.clinicapp.ui.home.PatientClassificationFragment


class AboutFragment : BaseFragment(){


    private val model: SharedViewModel by activityViewModels()
    private var studyFormDetail: StudyFormDetail? = null
    private lateinit var progressBar: ProgressBar
    var integerListForModel = arrayListOf<Int>()
    var studyFormData:  List<StudyFormVariablesDao.StudyFormWithVariable> = emptyList()
    val variableValuesForModels = LinkedHashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studyFormDetail = model.selected.value

    }

    @SuppressLint("StringFormatInvalid", "CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_about_study_form, container, false)
        val masterID = arguments?.getParcelable<MasterStudyData>("masterID")


        view.txtHeader.text = getString(R.string.about_study_data)
        view.btn_confirm.visibility = View.VISIBLE
        progressBar = view.aboutProgressBar
        view.btn_confirm.text= getString(R.string.continue_str)
        view.btn_use_model.visibility = View.VISIBLE
        view.btn_use_model.text = "Apply Model"
        if(studyFormDetail!=null){
            view.about_study_form_details_title.text = studyFormDetail?.masterStudyForms?.title
            view.study_form_details_desc.text = studyFormDetail?.masterStudyForms?.description
        }

        if (masterID?.studyDataWhenAsked == Constants.StudyDataType.NEW_PATIENT_STUDY_DATA)
            integerListForModel = arrayListOf(0, 1)
        else if (masterID?.studyDataWhenAsked == Constants.StudyDataType.FOLLOWUP_STUDY_DATA)
            integerListForModel = arrayListOf(1)
        else if (masterID?.studyDataWhenAsked == Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA)
            integerListForModel = arrayListOf(1, 2)

        println("integerListForModel DATA4MODEL is " + integerListForModel)

        model.getMasterVariablesByFormVariablesForModel(
            model.variableValues.keys.toList(),
            model.selected.value?.masterStudyForms!!.tempMasterStudyFormsId!!,
            integerListForModel
        )

        model.setCustomObjectListener(object : SharedViewModel.MyCustomObjectListener {
            override fun onDataForModelReceived(data: List<StudyFormVariablesDao.StudyFormWithVariable>) {
                studyFormData = data
                println("Listener CALLED: Here are the values $data")
                model.variableValues.forEach { (key, value) ->
                // Perform operations with key and value
//                    println("KeyVariableValues: $key, Value: $value")
                    for (item in data){
                        if (key == item.formVariables.tempStudyFormVariablesId){
                            println("Key_name_is: ${item.masterVariables.variableName}, Value: $value")
                            variableValuesForModels[item.masterVariables.variableName] = value
                        }
                    }
                }
                println("Here are the values in a HashMap $variableValuesForModels")
            }
        })

        view.btn_confirm.setOnClickListener {
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.add(R.id.flAddMoreVars, FillStudiesFragment.newInstance(),"Fill_data")
            transaction?.addToBackStack("about")
            transaction?.commit()
        }

        view.btn_use_model.setOnClickListener {

            var selectedFilePath = filePaths[0]
            var selectedFileIndex = 0

            var userFilesNames = emptyArray<String>()
            var userFilePaths = emptyArray<String>()

            var size = fileNames.size
            Log.e("TAG", fileFullNames[0])

            for (i in 0 until size) {
                if (email in fileFullNames[i]) {
                    userFilesNames = userFilesNames.plus(fileNames[i])
                    userFilePaths = userFilePaths.plus(filePaths[i])
                }
            }

            try {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Choose a model")
                    .setSingleChoiceItems(userFilesNames, selectedFileIndex) {dialog, which ->
                        selectedFileIndex = which
                        selectedFilePath = userFilePaths[which]
                        Log.e("TAG", selectedFilePath)
                    }
                    .setPositiveButton("OK") { dialog, which ->
                        // transition to fragment that displays model output
                        // take all inputs to new fragment
                        val transaction = this.activity?.supportFragmentManager?.beginTransaction()
                        transaction?.addToBackStack("about")
                        Log.d("APPLY MODEL", "apply model button has been clicked")
                        if (variableValuesForModels.isNotEmpty()){
                            println("SENDING TO MODEL: Here are the values $variableValuesForModels")
                            transaction?.replace(R.id.flAddMoreVars, PatientClassificationFragment.newInstance(studyFormDetail, variableValuesForModels))
                            transaction?.commit()
                        }

                    }

                    .setNeutralButton("CANCEL") {dialog, which ->

                    }.show()
            }

            catch(ex: Exception){
                val dialogBuilder = AlertDialog.Builder(requireActivity())

                // set message of alert dialog
                dialogBuilder.setMessage("You have not imported any models from your Drive. Please import a model and then try again.")
                    .setCancelable(true)

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("No models imported.")
                // show alert dialog
                alert.show()
            }

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

    private fun getNameAndValue(key1: String, value: String, itemVariables: List<StudyFormVariablesDao.StudyFormWithVariable>){
//        println("Key is: $key1 and Value is: $value")
//        for (item in itemVariables){
//            if (key1 == item.formVariables.tempStudyFormVariablesId)
//                println("Key_name_is: ${item.masterVariables.variableName}, Value: $value")
//        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance(masterID: MasterStudyData?) =
            AboutFragment().apply {
                arguments = Bundle().apply {
                    // Pass the MasterStudyData instance to the fragment
                    putParcelable("masterID", masterID)
                }

            }
    }
}
