package org.broadinstitute.clinicapp.data.source

import androidx.paging.DataSource
import org.broadinstitute.clinicapp.data.source.local.dao.MasterStudyFormsDao
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.dao.SyncStatusDao
import org.broadinstitute.clinicapp.data.source.local.entities.*
import org.broadinstitute.clinicapp.data.source.remote.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface ClinicDataSource {

    fun getMasterVariablesCount(): Single<Int>

    fun getMasterStudyFormsCount(): Single<Int>

    fun getMasterStudyDatasCount(masterStudyFormsId: String): Single<Int>

    fun getMasterVariablesFromAPI(
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<MasterVariablesResponse>

    fun getMasterVariablesByCategory(category: String): List<MasterVariables>

    fun getMasterStudyFormsFromDB(limit: Int, offset: Int): Flowable<List<StudyFormDetail>>

    fun getNewStudyFormsFromDB(): Single<List<MasterStudyForms>>

    fun getNewPatientsFromDB(): Single<List<Patient>>

    fun getNewStudyDataFromDB(adminId: String): List<MasterStudyData>

    /*fun searchStudyDataFromDB(
        search: String,
        tempId: String
    ): DataSource.Factory<Int, MasterStudyDataDao.MasterStudyDataWithSD>*/

    fun searchStudyDataFromDB(search: String, tempId: String): DataSource.Factory<Int, MasterStudyData>

    fun searchStudyDataFromDB(ids : List<String>): DataSource.Factory<Int, MasterStudyData>


    fun getSearchableVariables(
        tempId: String
    ): Single<List<String>>

    fun getSearchableValues(
        tempFormVariableIDs: List<String>
    ): Single<List<StudyData>>

    fun getUpdatedFormVariables(formId: String): List<StudyFormVariables>

    fun associateStudyForm(
        userID: String,
        tempMasterFormID: String
    ): Single<CreateStudyFormsResponse>

    fun getMyStudyFormsOnline(
        userId: String,
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyFormsResponse>

    fun createStudyFormsFromAPI(forms: List<MasterStudyForms>): Single<CreateStudyFormsResponse>

    fun createStudyDataFromAPI(studyDataRequest: StudyDataRequest): Single<CreateStudyFormsResponse>

    fun searchStudyFormsFromDB(
        query: String,
        limit: Int,
        offset: Int
    ): Flowable<List<StudyFormDetail>>

    fun searchStudyFormsOnline(
        query: String,
        pageSize: Int,
        pageNo: Int,
        excludeUserId: String?
    ): Single<StudyFormsResponse>

    fun getCategories(): Flowable<List<String>>

    fun checkFormTitle(title: String): Single<List<MasterStudyForms>>

    fun insertMasterVariables(masterVariablesList: List<MasterVariables>): List<Long>

    fun insertMasterStudyForm(masterStudyForms: MasterStudyForms): Long

    fun insertSingleStudyForm(masterStudyForms: MasterStudyForms): Single<Long>

    fun updateMasterStudyForm(tempId: String): Int

    fun updateStudyFormVariables(formId: String): Int

    fun updateMasterStudyData(tempId: String): Int

    fun insertStudyFormVariables(studyFormVariablesList: List<StudyFormVariables>): List<Long>

    fun insertStudyFormVariable(studyFormVariables: StudyFormVariables): Long

    fun insertMasterStudyData(masterStudyData: MasterStudyData): Long

    fun insertMasterSingleStudyData(masterStudyData: MasterStudyData): Single<Long>

    fun insertStudyData(studyDataList: List<StudyData>): List<Long>

    fun insertStudyData(studyData: StudyData): Long

    fun insertPatientSingle(patient: Patient): Single<Long>

    fun insertPatient(patient: Patient): Long

    fun getStudyDataOnline(
        userId: String,
        studyFormId: String,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyDataResponse>

    fun getStudyDataFromDB(
        studyFormId: String
    ): DataSource.Factory<Int, MasterStudyData>

    fun getStudyData(masterId: String): List<StudyData>

    fun getPatients(
        search: String,
        studyFormId: String
    ): DataSource.Factory<Int, Patient>

    fun getStudyDataSingle(masterId: String): Single<List<StudyData>>

    fun getPatient(patientID: String): Single<Patient>

    fun updateUser(user: User): Observable<UserResponse>

    fun deleteFormsAndStudyData(): Single<Int>

    fun insertSyncStatus(syncStatus: SyncStatus): Long

    fun getLastModifiedForMV(): Long

    fun getLastModifiedForMSF(): Long

    fun getLastModifiedForMSD(): Long

    fun searchStudyFormsDBCount(query: String): Single<Int>

    fun getStudyAllDataOnline(
        userId: String,
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyDataResponse>

    fun getAllLastSyncDate(): Single<SyncStatusDao.AllLastSyncDate>

    fun deleteAllMasterStudyData(): Int

    fun deleteAllPatients(): Int

    fun deleteAllFormVariable(): Int

    fun deleteAllForms(): Int

    fun deleteAllSyncForms(): Int

    fun getFormVariables(
        id: String,
        studyType: List<Int>
    ): Single<List<StudyFormVariablesDao.StudyFormWithVariable>>

    fun getFormVariables(
        tempFormVariableIDs: List<String>,
        formId: String, studyType: List<Int>
    ): Single<List<StudyFormVariablesDao.StudyFormWithVariable>>

    fun getMasterVariableById(ids: Long): Single<MasterVariables>

    fun searchStudyFormsFromDBItem(query: String): DataSource.Factory<Int, StudyFormDetail>
    fun getAllUnSyncCounts(): Single<MasterStudyFormsDao.UnSyncCount>
    fun getSearchVariables(): Single<List<Long>>

}