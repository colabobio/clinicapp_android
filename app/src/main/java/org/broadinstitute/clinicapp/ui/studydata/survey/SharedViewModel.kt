package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
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

    val selected = MutableLiveData<StudyFormDetail>()
    val studyDataType = MutableLiveData<Int>()
    val masterStudyData = MutableLiveData<MasterStudyData>()
    val patient = MutableLiveData<Patient>()

    var list = arrayListOf<StudyFormVariablesDao.StudyFormWithVariable>()
    val variableValues = LinkedHashMap<String, String>()
    var dataList = arrayListOf<StudyData>()

    var source = PublishSubject.create<Boolean>()

    fun setType(t: Int, pref: SharedPreferenceUtils) {
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
        selected.value = item
    }

    fun setMasterStudyDataID(item: MasterStudyData) {
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
        patient.value = item
    }

    private fun createPatient(pref: SharedPreferenceUtils): Patient {
        val tempId =
            CommonUtils.generateAdminId(pref.readLongFromPref(Constants.PrefKey.PREF_USER_ID))
        return Patient(
            adminId = tempId
        )
    }

    @SuppressLint("CheckResult")
    private fun getMasterVariables(studyFromID: String, studyType: List<Int>) {

        compositeDisposable.add(repository.getFormVariables(studyFromID, studyType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                list = data as ArrayList<StudyFormVariablesDao.StudyFormWithVariable>
                if (studyDataType.value != Constants.StudyDataType.UPDATE_STUDY_DATA) {
                    data.forEach {
                        variableValues[it.formVariables.tempStudyFormVariablesId] = ""
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

        source.onNext(true)
        compositeDisposable.add(repository.getStudyDataSingle(masterStudyDataID)

            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
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
        compositeDisposable.clear()
    }


}