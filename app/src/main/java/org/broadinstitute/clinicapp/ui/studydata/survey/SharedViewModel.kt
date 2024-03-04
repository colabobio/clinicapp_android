package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyData
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils


class SharedViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository by lazy {
        ClinicRepository.getInstance(ClinicApp.applicationContext())
    }
    //=============== LISTENERS TO SEND THE DATA TO ABOUT FRAGMENT ==========
    interface MyCustomObjectListener {
        fun onDataForModelReceived(data: List<StudyFormVariablesDao.StudyFormWithVariable>)
    }

    // Member variable was defined earlier
    private var listener: MyCustomObjectListener? = null

    fun setCustomObjectListener(listener: MyCustomObjectListener?) {
        this.listener = listener
    }

    val selected = MutableLiveData<StudyFormDetail>()
    val studyDataType = MutableLiveData<Int>()
    val masterStudyData = MutableLiveData<MasterStudyData>()
    val patient = MutableLiveData<Patient>()

    var listForVariableValues: List<StudyData>? = null
    var list = arrayListOf<StudyFormVariablesDao.StudyFormWithVariable>()

    val variableValues = LinkedHashMap<String, String>()
    var dataList = arrayListOf<StudyData>()

    var dataForModel = listOf<StudyFormVariablesDao.StudyFormWithVariable>()

    var source = PublishSubject.create<Boolean>()

    fun setType(t: Int, pref: SharedPreferenceUtils) {
        println("setType method is called")
        studyDataType.value = t
        when (t) {
            Constants.StudyDataType.NEW_PATIENT_STUDY_DATA -> {
                patient.value = createPatient(pref)
                selected.value?.masterStudyForms?.tempMasterStudyFormsId?.let {
                    getMasterVariables(
                        it,
                        arrayListOf(0, 1)
                    )
                }
            }
            Constants.StudyDataType.FOLLOWUP_STUDY_DATA -> selected.value?.masterStudyForms?.tempMasterStudyFormsId?.let {
//                println("FOLLOW Up TYPE is: $selected.value?.masterStudyForms?.tempMasterStudyFormsId")
                getMasterVariables(
                    it,
                    arrayListOf(1)
                )
            }
            Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA -> selected.value?.masterStudyForms?.tempMasterStudyFormsId?.let {
                getMasterVariables(
                    it,
                    arrayListOf(1, 2)
                )
            }
        }
    }

    fun select(item: StudyFormDetail) {
        println("select method is called")
        selected.value = item
    }

    fun setMasterStudyDataID(item: MasterStudyData) {
        println("setMasterStudyDataID method is called")
        masterStudyData.value = item
        when (item.studyDataWhenAsked) {
            Constants.StudyDataType.NEW_PATIENT_STUDY_DATA -> getStudyData(
                item.tempMasterStudyDataId,
                arrayListOf(0, 1)
            )
            Constants.StudyDataType.FOLLOWUP_STUDY_DATA -> getStudyData(
                item.tempMasterStudyDataId,
                arrayListOf(1)
            )
            Constants.StudyDataType.FINAL_OUTCOME_STUDY_DATA -> getStudyData(
                item.tempMasterStudyDataId,
                arrayListOf(1, 2)
            )
        }
    }

    fun setPatient(item: Patient) {
        println("setPatient method is called")
        patient.value = item
    }

    private fun createPatient(pref: SharedPreferenceUtils): Patient {
        println("createPatient method is called")
        val tempId =
            CommonUtils.generateAdminId(pref.readLongFromPref(Constants.PrefKey.PREF_USER_ID))
        return Patient(
            adminId = tempId
        )
    }

    @SuppressLint("CheckResult")
    private fun getMasterVariables(studyFromID: String, studyType: List<Int>) {

        println("getMasterVariables method is called")

        compositeDisposable.add(repository.getFormVariables(studyFromID, studyType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                list = data as ArrayList<StudyFormVariablesDao.StudyFormWithVariable>
                println("getMasterVariables list is:, ${list}")
                if (studyDataType.value != Constants.StudyDataType.UPDATE_STUDY_DATA) {
                    data.forEach {
                        Log.d("THe datalist is:", it.toString() )
                        variableValues[it.formVariables.tempStudyFormVariablesId] = ""
                        Log.d("variableValues is:", variableValues[it.formVariables.tempStudyFormVariablesId].toString() )
                    }
                }
            },
                {
                    //onError(throwable.localizedMessage!!)
                }
            ))
    }


    @SuppressLint("CheckResult")
    private fun getMasterVariablesByFormVariables(
        variablesIds: List<String>,
        formId: String,
        studyType: List<Int>
    ) {
        println("getMasterVariablesByFormVariables method is called")

        compositeDisposable.add(repository.getFormVariables(variablesIds, formId, studyType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                list = data as ArrayList<StudyFormVariablesDao.StudyFormWithVariable>
            },
                { throwable ->
                    throwable.stackTrace
                }
            ))
    }


    @SuppressLint("CheckResult")
    fun getStudyData(masterStudyDataID: String, studyType: List<Int>) {
        println("getStudyData method is called")

        source.onNext(true)
        compositeDisposable.add(repository.getStudyDataSingle(masterStudyDataID)

            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                listForVariableValues = it
                dataList = it as ArrayList<StudyData>
                println("Get Start time :: " + System.currentTimeMillis() )
                dataList.forEach { t ->
                    val temp = t.variableValue
                    variableValues[t.tempStudyFormVariablesId]= temp
                }

                println("Get End time :: " + System.currentTimeMillis() )
                source.onNext(false)
                // If any update in study form like remove or update variables but still study data has collected
                // deleted recorded so those only variables will be fetched.
                println("SHRDVM_VARIABLEz KEYS is " + variableValues.keys.toList())
                println("SHRDVM_VARIABLEz tempMasterStudyFormsId is " + selected.value?.masterStudyForms!!.tempMasterStudyFormsId)
                println("SHRDVM_VARIABLEz STUDYTYPE is " + studyType)

                getMasterVariablesByFormVariables(
                    variableValues.keys.toList(),
                    selected.value?.masterStudyForms!!.tempMasterStudyFormsId!!,
                    studyType
                )
            }
            ) {
                source.onNext(false)
            })


    }

    @SuppressLint("CheckResult")
    fun getPatient(adminID: String) {
        println("getPatient method is called")

        compositeDisposable.add(repository.getPatient(adminID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                patient.value = it
            }
            ) {

            })
    }

    fun clearDisposable() {
        println("clearDisposable method is called")
        compositeDisposable.clear()
    }
//============================================== ADDED FOR THE MY MODELS SECTION =====================

    @SuppressLint("CheckResult")
    fun getMasterVariablesByFormVariablesForModel(
        variablesIds: List<String>,
        formId: String,
        studyType: List<Int>
    ){
        println("getMasterVariablesByFormVariablesForModel method is called")
        compositeDisposable.add(repository.getFormVariables(variablesIds, formId, studyType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                // Add this line to check if the block is executed
                Log.d("Inside subscribe block", "Data received: $data")

                list = data as ArrayList<StudyFormVariablesDao.StudyFormWithVariable>
                Log.d("list is Called ", list.toString())
                dataForModel = data
                listener?.onDataForModelReceived(dataForModel)
            },
                { throwable ->
                    Log.e("Error in getMasterVariablesByFormVariablesForModel", throwable.message ?: "Unknown error")
                    throwable.printStackTrace()
                    throwable.stackTrace
                }
            ))

    }

}

