package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_note_screen.view.*
import kotlinx.android.synthetic.main.survey_notes.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyData
import org.broadinstitute.clinicapp.util.CommonUtils
import java.util.*


class NoteFragment : BaseFragment(), NoteContract.View {
    private lateinit var noteLayout: View
    private val model: SharedViewModel by activityViewModels()
    private lateinit var presenter: NotePresenter

    private val studyForms by lazy {
        model.selected.value?.masterStudyForms
    }

    private val masterStudyData by lazy {
        model.masterStudyData.value
    }

    private val patient by lazy {
        model.patient.value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = NotePresenter(this, requireContext())
    }

    @SuppressLint("StringFormatInvalid")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_note_screen, container, false)

        view.txtHeader.text = getString(R.string.complete_survey)
        noteLayout = view.complete_surveyNoteLayout
        //    thankYouLayout = view.complete_surveyThanksLayout
        noteLayout.visibility = View.VISIBLE
        view.fill_noteTxt.setText(masterStudyData?.note)
        val dataType = model.studyDataType.value

        noteLayout.btn_complete_survey.setOnClickListener {
            val notes = view.fill_noteTxt.text.toString().trim()
          //  if (notes.isNotBlank()) {
                noteLayout.isEnabled = false

                if (dataType == Constants.StudyDataType.UPDATE_STUDY_DATA) {

                    val m = updateMasterStudyData(notes)
                    val s = updateStudyData()
                    // Update case pass master study data when asked value to create demographic function
                    // otherwise pass value from model.studyDataType
                    patient?.demographics = createDemographic(masterStudyData?.studyDataWhenAsked)
                    m?.let { it1 ->
                        patient?.let { it2 ->
                            presenter.insertStudyData(
                                it2,
                                it1,
                                s
                            )
                        }
                    }
                } else {
                    //for (i in 1..10){
//                        val patient = model.createPatient(pref)
//                        patient.demographics = this.patient?.demographics.toString()
//                        val m = createMasterStudyData(notes, patient)
//                        val s = createNewStudyData(m.tempMasterStudyDataId)

                        patient?.let {
                            it.demographics = createDemographic(model.studyDataType.value)
                            val m = createMasterStudyData(notes, it)
                            val s = createNewStudyData(m.tempMasterStudyDataId)
                            presenter.insertStudyData(it, m, s)
                        }

                }
           /* } else {
                onError(getString(R.string.provide_inputs))
            }*/

        }
        return view
    }


    private fun createDemographic(value: Int?): String {
        if (value == null) return ""
        when (value) {
            Constants.StudyDataType.NEW_PATIENT_STUDY_DATA -> {
                val builder = StringBuilder()
                val filteredList =
                    model.list.filter { it.masterVariables.variableCategory.toLowerCase(Locale.ENGLISH) == "demographic" }
                filteredList.forEach { studyFormWithVariable ->

//                    demographicObj.addProperty(
//                        studyFormWithVariable.masterVariables.label,
//                        model.variableValues[studyFormWithVariable.formVariables.tempStudyFormVariablesId].toString() +
//                    )

                    builder.append(studyFormWithVariable.masterVariables.label +" : ")
                    builder.append(model.variableValues[studyFormWithVariable.formVariables.tempStudyFormVariablesId].toString() + "\n")


                }
            return builder.toString()
            }
            Constants.StudyDataType.FOLLOWUP_STUDY_DATA, Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA -> {
                return patient?.demographics!!
            }
            else -> return ""
        }
    }

    /**
     * MasterStudy data will be created only when user select options(New, FollowUp, Final) from after click on "+" icon of study data list
     *
     */
    private fun createMasterStudyData(note: String, patient: Patient): MasterStudyData {
        val tempId = CommonUtils.generateTempId(Constants.TempIdType.MASTER_STUDY_DATA_TEMP, userID)
        val currentTime = System.currentTimeMillis()

        return MasterStudyData(
            tempMasterStudyDataId = tempId,
            tempMasterStudyFormsId = studyForms?.tempMasterStudyFormsId.toString(),
            keycloakUserFk = userID,
            isServerUpdated = false,
            adminId = patient.adminId,
            latitude = pref.readFloatFromPref(Constants.PrefKey.PREF_LATITUDE).toDouble(),
            longitude = pref.readFloatFromPref(Constants.PrefKey.PREF_LONGITUDE).toDouble(),
            createdOn = currentTime,
            lastModified = currentTime,
            version = studyForms!!.version,
            timezone = CommonUtils.getTimezone(),
            note = note,
            studyDataWhenAsked = model.studyDataType.value!!.toInt()
        )
    }

    private fun updateMasterStudyData(note: String): MasterStudyData? {
        val currentTime = System.currentTimeMillis()
        masterStudyData?.lastModified = currentTime
        masterStudyData?.isServerUpdated = false

        masterStudyData?.latitude =
            pref.readFloatFromPref(Constants.PrefKey.PREF_LATITUDE).toDouble()
        masterStudyData?.longitude =
            pref.readFloatFromPref(Constants.PrefKey.PREF_LONGITUDE).toDouble()
        masterStudyData?.note = note

        model.dataList.forEach {
            val currentTimeInMillis = System.currentTimeMillis()
            it.lastModified = currentTimeInMillis
            it.variableValue = model.variableValues[it.tempStudyFormVariablesId].toString()
        }
        return masterStudyData
    }

    private fun createNewStudyData(masterDataID: String): List<StudyData> {

        val studyDataList = arrayListOf<StudyData>()

        var counter = 1
        println("Start time :: " + System.currentTimeMillis() )
        model.variableValues.forEach { masterVariableID ->

            val currentTimeInMillis = System.currentTimeMillis()
            val variable = StudyData()
            variable.tempStudyDataId =
                CommonUtils.generateTempId(
                    Constants.TempIdType.STUDY_DATA_TEMP,
                    userID, counter
                )
            variable.tempStudyFormVariablesId = masterVariableID.key
            variable.tempMasterStudyDataId = masterDataID
            variable.variableValue = masterVariableID.value
            variable.capturedOn = currentTimeInMillis
            variable.lastModified = currentTimeInMillis
            variable.timezone = CommonUtils.getTimezone()
            counter++
            studyDataList.add(variable)

        }
        println("End time :: " + System.currentTimeMillis() )
        return studyDataList
    }

    private fun updateStudyData(): List<StudyData> {

        val studyDataList = arrayListOf<StudyData>()
        println("Update Start time :: " + System.currentTimeMillis() )
        model.dataList.forEach {
            val currentTimeInMillis = System.currentTimeMillis()

            val input = model.variableValues[it.tempStudyFormVariablesId].toString()
            it.variableValue = input
            it.lastModified = currentTimeInMillis
            it.timezone = CommonUtils.getTimezone()
            studyDataList.add(it)
        }
        println("update end time :: " + System.currentTimeMillis() )
        return studyDataList

    }

    override fun studyDataInserted() {
        noteLayout.isEnabled = true
        val transaction = this.activity?.supportFragmentManager?.beginTransaction()
        this.activity?.supportFragmentManager?.popBackStack("about", 0)
        this.activity?.supportFragmentManager?.popBackStackImmediate()
        transaction?.replace(R.id.flAddMoreVars, CompleteFragment.newInstance(), "complete")
        transaction?.commit()
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else hideLoading()
    }

    override fun showSnackBarMessage(message: String) {
        noteLayout.isEnabled = true
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment NoteFragment.
         */
        @JvmStatic
        fun newInstance() =
            NoteFragment()

    }

}
