package org.broadinstitute.clinicapp.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.paging.DataSource
import com.google.gson.JsonObject
import org.broadinstitute.clinicapp.data.source.local.ClinicDatabase
import org.broadinstitute.clinicapp.data.source.local.dao.MasterStudyFormsDao
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.dao.SyncStatusDao
import org.broadinstitute.clinicapp.data.source.local.entities.*
import org.broadinstitute.clinicapp.data.source.remote.*
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.api.ClinicApiService


class ClinicRepository(
    val context: Context, storage: SharedPreferences?
) : ClinicDataSource {
    private val clinicDatabase: ClinicDatabase = ClinicDatabase.getInstance(this.context)
    private val apiService: ClinicApiService = ClinicApiService.create(storage)

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Local database source

    override fun getStudyDataFromDB(
        studyFormId: String
    ): DataSource.Factory<Int, MasterStudyData> {
        return clinicDatabase.getMasterStudyDataDao().getMasterStudyData(studyFormId)
    }

    override fun getStudySpecificDataFromDB(
        studyFormId: String, masterStudyDataID: String
    ): DataSource.Factory<Int, MasterStudyData> {
        return clinicDatabase.getMasterStudyDataDao().getSpecificMasterStudyData(studyFormId, masterStudyDataID)
    }

    override fun checkFormTitle(title: String): Single<List<MasterStudyForms>> {
        return clinicDatabase.getMasterStudyFormsDao().checkTitle(title)
    }

    override fun getNewStudyFormsFromDB(): Single<List<MasterStudyForms>> {
        return clinicDatabase.getMasterStudyFormsDao().getUnSyncedStudyForms()
    }

    override fun getNewPatientsFromDB(): Single<List<Patient>> {
        return clinicDatabase.getPatientDao().getUnSyncedPatient()
    }

    override fun getNewStudyDataFromDB(adminId: String): List<MasterStudyData> {
        return clinicDatabase.getMasterStudyDataDao().getUnSyncedStudyData(adminId)
    }

    override fun searchStudyDataFromDB(
        search: String,
        tempId: String
    ): DataSource.Factory<Int, MasterStudyData> {
        return clinicDatabase.getMasterStudyDataDao().searchStudyData(search, tempId)
    }

    override fun searchStudyDataFromDB(ids: List<String>): DataSource.Factory<Int, MasterStudyData> {
        return clinicDatabase.getMasterStudyDataDao().searchStudyData(ids)
    }

    override fun getSearchableVariables(
        tempId: String
    ): Single<List<String>> {
        return clinicDatabase.getStudyFormVariablesDao().getSearchableFormVariables(tempId)
    }

    override fun getSearchableValues(
        tempFormVariableIDs: List<String>
    ): Single<List<StudyData>> {
        return clinicDatabase.getStudyDataDao().getSearchableValues(tempFormVariableIDs)
    }

    override fun getUpdatedFormVariables(formId: String): List<StudyFormVariables> {
        return clinicDatabase.getStudyFormVariablesDao().getUpdatedFormVariables(formId)
    }

    override fun searchStudyFormsFromDB(
        query: String,
        limit: Int,
        offset: Int
    ): Flowable<List<StudyFormDetail>> {
        return clinicDatabase.getMasterStudyFormsDao().searchStudyForms(query, limit, offset)
    }

    override fun searchStudyFormsDBCount(
        query: String
    ): Single<Int> {
        return clinicDatabase.getMasterStudyFormsDao().searchStudyFormsCount(query)
    }

    override fun getMasterStudyFormsFromDB(
        limit: Int,
        offset: Int
    ): Flowable<List<StudyFormDetail>> {
        return clinicDatabase.getMasterStudyFormsDao().getAllMasterStudyForms(limit, offset)
    }

    override fun searchStudyFormsFromDBItem(query: String): DataSource.Factory<Int, StudyFormDetail> {
        return clinicDatabase.getMasterStudyFormsDao().searchStudyFormsItems(query)
    }

    override fun insertMasterStudyForm(masterStudyForms: MasterStudyForms): Long {
        return clinicDatabase.getMasterStudyFormsDao().insert(masterStudyForms)
    }

    override fun insertSingleStudyForm(masterStudyForms: MasterStudyForms): Single<Long> {
        return clinicDatabase.getMasterStudyFormsDao().insertSingle(masterStudyForms)
    }

    override fun updateMasterStudyForm(tempId: String): Int {
        return clinicDatabase.getMasterStudyFormsDao()
            .updateMasterStudyForm(tempId)
    }

    override fun updateStudyFormVariables(formId: String): Int {
        return clinicDatabase.getStudyFormVariablesDao()
            .updateStudyFormVariables(formId)
    }

    override fun updateMasterStudyData(tempId: String): Int {
        return clinicDatabase.getMasterStudyDataDao().updateMasterStudyData(tempId)
    }

    override fun getCategories(): Flowable<List<String>> {
        return clinicDatabase.getMasterVariablesDao().getAllCategories()
    }

    override fun getMasterVariablesByCategory(category: String): List<MasterVariables> {
        return clinicDatabase.getMasterVariablesDao().getMasterVariables(category)
    }

    override fun getMasterVariablesCount(): Single<Int> {
        return clinicDatabase.getMasterVariablesDao().getCount()
    }

    override fun getSearchVariables(): Single<List<Long>> {
        return clinicDatabase.getMasterVariablesDao().getSearchVariables()
    }

    override fun getMasterStudyFormsCount(): Single<Int> {
        return clinicDatabase.getMasterStudyFormsDao().getCount()
    }

    override fun getMasterStudyDatasCount(masterStudyFormsId: String): Single<Int> {
        return clinicDatabase.getMasterStudyDataDao().getCountByStudyForm(masterStudyFormsId)
    }

    override fun getMasterStudyDatasSpecificCount(masterStudyFormsId: String, masterStudyDataID: String): Single<Int> {
        return clinicDatabase.getMasterStudyDataDao().getCountByStudySpecificForm(masterStudyFormsId, masterStudyDataID)
    }

    override fun insertMasterVariables(masterVariablesList: List<MasterVariables>): List<Long> {
        return clinicDatabase.getMasterVariablesDao().insertAll(masterVariablesList)
    }

    override fun getMasterVariableById(ids: Long): Single<MasterVariables> {
        return clinicDatabase.getMasterVariablesDao().getVariableByID(ids)
    }

    override fun insertStudyFormVariables(studyFormVariablesList: List<StudyFormVariables>): List<Long> {
        return clinicDatabase.getStudyFormVariablesDao().insertAll(studyFormVariablesList)
    }

    override fun insertStudyFormVariable(studyFormVariables: StudyFormVariables): Long {
        return clinicDatabase.getStudyFormVariablesDao().insert(studyFormVariables)
    }

    override fun getFormVariables(
        id: String,
        studyType: List<Int>
    ): Single<List<StudyFormVariablesDao.StudyFormWithVariable>> {
        return clinicDatabase.getStudyFormVariablesDao()
            .getStudyFormWithDetailVariable(id, studyType)
    }

    override fun getFormVariables(
        tempFormVariableIDs: List<String>,
        formId: String, studyType: List<Int>
    ): Single<List<StudyFormVariablesDao.StudyFormWithVariable>> {
        return clinicDatabase.getStudyFormVariablesDao()
            .getStudyFormWithDetailVariable(tempFormVariableIDs, formId, studyType)
    }

    override fun insertMasterStudyData(masterStudyData: MasterStudyData): Long {
        return clinicDatabase.getMasterStudyDataDao().insert(masterStudyData)
    }

    override fun insertMasterSingleStudyData(masterStudyData: MasterStudyData): Single<Long> {
        return clinicDatabase.getMasterStudyDataDao().insertSingle(masterStudyData)
    }

    override fun insertStudyData(studyDataList: List<StudyData>): List<Long> {
        return clinicDatabase.getStudyDataDao().insertAll(studyDataList)
    }

    override fun insertStudyData(studyData: StudyData): Long {
        return clinicDatabase.getStudyDataDao().insert(studyData)
    }

    override fun insertPatientSingle(patient: Patient): Single<Long> {
        return clinicDatabase.getPatientDao().insertPatientSingle(patient)
    }

    override fun insertPatient(patient: Patient): Long {
        return clinicDatabase.getPatientDao().insertPatient(patient)
    }

    override fun getStudyData(masterId: String): List<StudyData> {
        return clinicDatabase.getStudyDataDao().getStudyData(masterId)
    }

    override fun getPatients(
        search: String,
        studyFormId: String
    ): DataSource.Factory<Int, Patient> {
        return clinicDatabase.getPatientDao().getPatients(search, studyFormId)
    }

    override fun getSpecificPatient(
        studyFormId: String
    ): DataSource.Factory<Int, Patient> {
        return clinicDatabase.getPatientDao().getSpecificPatient(studyFormId)
    }

    override fun getStudyDataSingle(masterId: String): Single<List<StudyData>> {
        return clinicDatabase.getStudyDataDao().getStudyDataSingle(masterId)
    }

    override fun getPatient(patientID: String): Single<Patient> {
        return clinicDatabase.getPatientDao().getPatient(patientID)
    }

    override fun deleteFormsAndStudyData(): Single<Int> {
        return clinicDatabase.getStudyDataDao().deleteAllStudyData()
    }

    override fun deleteAllMasterStudyData(): Int {
        return clinicDatabase.getMasterStudyDataDao().deleteAllMasterStudyData()
    }

    override fun deleteAllPatients(): Int {
        return clinicDatabase.getPatientDao().deleteAllPatient()
    }

    override fun deleteAllFormVariable(): Int {
        return clinicDatabase.getStudyFormVariablesDao().deleteAllVariable()
    }

    override fun deleteAllForms(): Int {
        return clinicDatabase.getMasterStudyFormsDao().deleteStudyForms()
    }

    override fun deleteAllSyncForms(): Int {
        return clinicDatabase.getSyncStatusDao().deleteAll()
    }

    override fun insertSyncStatus(syncStatus: SyncStatus): Long {
        return clinicDatabase.getSyncStatusDao().insertSyncStatus(syncStatus)
    }

    override fun getLastModifiedForMSD(): Long {
        return clinicDatabase.getMasterStudyDataDao().getLastModified()
    }

    override fun getLastModifiedForMSF(): Long {
        return clinicDatabase.getMasterStudyFormsDao().getLastModified()
    }

    override fun getLastModifiedForMV(): Long {
        return clinicDatabase.getMasterVariablesDao().getLastModified()
    }

    override fun getAllLastSyncDate(): Single<SyncStatusDao.AllLastSyncDate> {
        return clinicDatabase.getSyncStatusDao().getAllLastModified()
    }


    override fun getAllUnSyncCounts(): Single<MasterStudyFormsDao.UnSyncCount> {
        return clinicDatabase.getMasterStudyFormsDao().getUnSyncCounts()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Remote source

    override fun getStudyDataOnline(
        userId: String,
        studyFormId: String,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyDataResponse> {
        return apiService.getStudyDataByFormId(userId, studyFormId, pageSize, pageNo)
    }

    override fun getStudyAllDataOnline(
        userId: String,
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyDataResponse> {
        return apiService.getStudyAllData(userId, lastModified, pageSize, pageNo)
    }

    override fun associateStudyForm(
        userID: String,
        tempMasterFormID: String
    ): Single<CreateStudyFormsResponse> {

        val jsonObject = JsonObject()
        jsonObject.addProperty("userDetailsKeycloakUser", userID)
        jsonObject.addProperty("tempMasterStudyFormsId", tempMasterFormID)
        return apiService.associateStudyForm(jsonObject)
    }


    override fun createStudyFormsFromAPI(
        forms: List<MasterStudyForms>
    ): Single<CreateStudyFormsResponse> {
        return apiService.submitStudyForms(forms)
    }

    override fun createStudyDataFromAPI(studyDataRequest: StudyDataRequest): Single<CreateStudyFormsResponse> {
        return apiService.submitStudyData(studyDataRequest)
    }

    override fun searchStudyFormsOnline(
        query: String,
        pageSize: Int,
        pageNo: Int,
        excludeUserId: String?
    ): Single<StudyFormsResponse> {
        return apiService.searchStudyForms(query, pageSize, pageNo, excludeUserId)
    }

    override fun getMyStudyFormsOnline(
        userId: String,
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<StudyFormsResponse> {
        return apiService.getMyStudyForms(userId, lastModified, pageSize, pageNo)
    }

    override fun getMasterVariablesFromAPI(
        lastModified: Long,
        pageSize: Int,
        pageNo: Int
    ): Single<MasterVariablesResponse> {
        return apiService.getMasterVariables(lastModified, pageSize, pageNo)
    }

    override fun updateUser(user: User): Observable<UserResponse> {
        return apiService.createUser(user)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    companion object {

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: ClinicRepository? = null

        @JvmStatic
        fun getInstance(
            context: Context
        ) =
            INSTANCE
                ?: synchronized(ClinicRepository::class.java) {
                val token = ClinicApp.instance?.getStorage()
                INSTANCE
                    ?: ClinicRepository(context, token)
                    .also { INSTANCE = it }
            }



    }
}