package org.broadinstitute.clinicapp.ui.studydata.list

import androidx.paging.PagedList
import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient


class SDListContract {

    interface View : BaseContract.View {

        fun showStudyData(list: PagedList<MasterStudyData>)
        fun showProgressBar(isShow: Boolean)
        fun showEmptyWarning(isEmpty: Boolean)
        fun showPatients(patientList: PagedList<Patient>)
        fun initializePatients(patients: Patient)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun searchStudyDataFromDB(search: String, tempId: String)
        fun getStudyDataByFormIdFromAPI(userId: String, studyFormId: String)
        fun getStudyDataFromDB(studyFormId: String, search: String)
        fun checkStudyDataInDB(studyFormId: String, isServerUpdate: Boolean)
        fun getPatients(search: String, studyFormId: String)
        fun getStudyDataSpecificFromDB(studyFormId: String, masterDtudyID: String, search: String)
        fun checkStudyDataSpecificInDB(studyFormId: String,masterStudyDataID: String, isServerUpdate: Boolean)

    }
}