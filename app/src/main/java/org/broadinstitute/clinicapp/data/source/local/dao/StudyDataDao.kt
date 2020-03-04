package org.broadinstitute.clinicapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.broadinstitute.clinicapp.data.source.local.entities.StudyData
import io.reactivex.Single


@Dao
interface StudyDataDao {
    /**
     * Insert a list in the database. If the item already exists, replace it.
     *
     * @param studyDataList to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAll(studyDataList: List<StudyData>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insert(studyData: StudyData): Long

    @Query("SELECT * FROM StudyData WHERE temp_master_study_data_id = :masterStudyDataId")
    fun getStudyData(masterStudyDataId: String): List<StudyData>

    @Query("SELECT * FROM StudyData WHERE temp_master_study_data_id = :masterStudyDataId")
    fun getStudyDataSingle(masterStudyDataId: String): Single<List<StudyData>>

    @Query("SELECT * FROM StudyData SD WHERE SD.temp_study_form_variables_id IN(:tempFormVariableIDs) Order by SD.last_modified DESC LIMIT 5000")
    fun getSearchableValues(
        tempFormVariableIDs: List<String>
    ): Single<List<StudyData>>

    @Query("DELETE FROM StudyData")
    fun deleteAllStudyData(): Single<Int>

}