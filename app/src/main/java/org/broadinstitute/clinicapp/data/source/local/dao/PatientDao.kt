package org.broadinstitute.clinicapp.data.source.local.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import io.reactivex.Single


@Dao
interface PatientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertPatientSingle(patient: Patient): Single<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertPatient(patient: Patient): Long

    @Query(
        "SELECT P.* FROM Patient P INNER JOIN MasterStudyData M ON P.admin_id = M.admin_id " +
                "AND M.temp_master_study_forms_id = :studyFormId AND M.study_data_when_asked = 0 " +
                "WHERE lower(P.admin_id) LIKE '%' || :search|| '%' OR lower(P.demographics) LIKE '%' || :search || '%'"
    )
    fun getPatients(search: String, studyFormId: String): DataSource.Factory<Int, Patient>

    @Query(
        "SELECT P.* FROM Patient P INNER JOIN MasterStudyData M ON P.admin_id = M.admin_id " +
                "AND M.temp_master_study_forms_id = :studyFormId AND M.study_data_when_asked = 0 " +
                "WHERE P.admin_id = :studyFormId"
    )
    fun getSpecificPatient(studyFormId: String): DataSource.Factory<Int, Patient>

    @Query(
        "SELECT DISTINCT(P.admin_id), P.demographics, P.patient_id FROM Patient P INNER JOIN MasterStudyData MSD ON P.admin_id = MSD.admin_id " +
                "INNER JOIN MasterStudyForms MSF ON MSF.temp_master_study_forms_id = MSD.temp_master_study_forms_id " +
                "AND MSD.is_server_updated = 0 AND MSF.is_server_updated = 1"
    )
    fun getUnSyncedPatient(): Single<List<Patient>>


    @Query("SELECT * FROM Patient WHERE admin_id = :adminId")
    fun getPatient(adminId: String): Single<Patient>

    @Query("DELETE FROM Patient")
    fun deleteAllPatient(): Int


}