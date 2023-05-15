package org.broadinstitute.clinicapp.data.source.local.dao

import androidx.paging.DataSource
import androidx.room.*
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import io.reactivex.Single


@Dao
interface MasterStudyDataDao {
    /**
     * Insert a list in the database. If the item already exists, replace it.
     *
     * @param masterStudyData to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insert(masterStudyData: MasterStudyData): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertSingle(masterStudyData: MasterStudyData): Single<Long>

    @Query("UPDATE MasterStudyData SET is_server_updated = 1 WHERE temp_master_study_data_id = :tempId")
    fun updateMasterStudyData(tempId: String): Int

    @Query("SELECT * FROM MasterStudyData WHERE temp_master_study_forms_id = :masterStudyFormsId AND study_data_when_asked = 0 ORDER BY created_on DESC")
    fun getMasterStudyData(masterStudyFormsId: String): DataSource.Factory<Int, MasterStudyData>

    @Query("SELECT * FROM MasterStudyData WHERE temp_master_study_forms_id = :masterStudyFormsId AND admin_id = :masterStudyDataID ORDER BY created_on DESC")
    fun getSpecificMasterStudyData(masterStudyFormsId: String, masterStudyDataID: String): DataSource.Factory<Int, MasterStudyData>

    @Query("SELECT * FROM MasterStudyData WHERE admin_id = :adminId AND is_server_updated = 0")
    fun getUnSyncedStudyData(adminId: String): List<MasterStudyData>

    @Query(
        "SELECT DISTINCT(MSD.master_study_data_id), MSD.* " +
                "FROM StudyData SD INNER JOIN StudyFormVariables AS SFV ON SD.temp_study_form_variables_id = SFV.temp_study_form_variables_id " +
                "AND lower(SD.variable_value) LIKE '%'||:search||'%' " +
                "INNER JOIN MasterStudyData MSD ON MSD.temp_master_study_forms_id = :tempId " +
                "AND MSD.temp_master_study_data_id = SD.temp_master_study_data_id " +
                "INNER JOIN MasterVariables M ON SFV.master_variables_id = M.master_variables_id " +
                "AND M.is_searchable = 1 AND SFV.temp_master_study_forms_id = :tempId " +
                "ORDER BY MSD.created_on DESC"
    )
    fun searchStudyData(search: String, tempId: String): DataSource.Factory<Int, MasterStudyData>

    @Query("SELECT * FROM MasterStudyData WHERE temp_master_study_data_id IN(:ids) ORDER BY MasterStudyData.created_on DESC")
    fun searchStudyData(ids: List<String>): DataSource.Factory<Int, MasterStudyData>

    @Query("SELECT count(*) FROM MasterStudyData WHERE temp_master_study_forms_id = :masterStudyFormsId")
    fun getCountByStudyForm(masterStudyFormsId: String): Single<Int>

    @Query("SELECT count(*) FROM MasterStudyData WHERE temp_master_study_forms_id = :masterStudyFormsId AND admin_id = :masterStudyDataID")
    fun getCountByStudySpecificForm(masterStudyFormsId: String, masterStudyDataID: String): Single<Int>

    @Query("DELETE FROM MasterStudyData")
    fun deleteAllMasterStudyData(): Int

    @Query("SELECT MAX(last_modified) FROM MasterStudyData")
    fun getLastModified(): Long
}