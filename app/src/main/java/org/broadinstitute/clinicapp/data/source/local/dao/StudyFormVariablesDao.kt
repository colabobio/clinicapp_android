package org.broadinstitute.clinicapp.data.source.local.dao

import android.os.Parcelable
import androidx.room.*
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormVariables
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize


const val SFV_PREFIX = "sfv_"

@Dao
interface StudyFormVariablesDao {
    /**
     * Insert a list in the database. If the item already exists, replace it.
     *
     * @param studyFormVariablesList to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAll(studyFormVariablesList: List<StudyFormVariables>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insert(studyFormVariables: StudyFormVariables): Long

    @Query("UPDATE StudyFormVariables SET is_server_updated = 1 WHERE temp_master_study_forms_id = :formId")
    fun updateStudyFormVariables(formId: String): Int

    @Query("SELECT * FROM StudyFormVariables WHERE temp_master_study_forms_id= :masterStudyFormsId AND is_server_updated = 0")
    fun getUpdatedFormVariables(masterStudyFormsId: String): List<StudyFormVariables>

    @Transaction
    @Query(
        "SELECT MasterVariables.*, " +
                "StudyFormVariables.temp_study_form_variables_id as " + SFV_PREFIX + "temp_study_form_variables_id, " +
                "StudyFormVariables.study_form_variables_id as " + SFV_PREFIX + "study_form_variables_id, " +
                "StudyFormVariables.temp_master_study_forms_id as " + SFV_PREFIX + "temp_master_study_forms_id, " +
                "StudyFormVariables.master_variables_id as " + SFV_PREFIX + "master_variables_id, " +
                "StudyFormVariables.created_on as " + SFV_PREFIX + "created_on, " +
                "StudyFormVariables.last_modified as " + SFV_PREFIX + "last_modified, " +
                "StudyFormVariables.timezone as " + SFV_PREFIX + "timezone, " +
                "StudyFormVariables.is_active as " + SFV_PREFIX + "is_active, " +
                "StudyFormVariables.is_server_updated as " + SFV_PREFIX + "is_server_updated " +
                "FROM MasterVariables " +
                "Inner Join StudyFormVariables on StudyFormVariables.master_variables_id = MasterVariables.master_variables_id " +
                "Inner join MasterStudyForms on MasterStudyForms.temp_master_study_forms_id = StudyFormVariables.temp_master_study_forms_id " +
                "where MasterStudyForms.temp_master_study_forms_id = :masterStudyFormsId AND StudyFormVariables.is_active = 1 " +
                "AND MasterVariables.when_asked IN(:studyType)" +
                "order by MasterVariables.variable_category Asc"
    )
    fun getStudyFormWithDetailVariable(
        masterStudyFormsId: String,
        studyType: List<Int>
    ): Single<List<StudyFormWithVariable>>

    @Transaction
    @Query(
        "SELECT MasterVariables.*, " +
                "StudyFormVariables.temp_study_form_variables_id as " + SFV_PREFIX + "temp_study_form_variables_id, " +
                "StudyFormVariables.study_form_variables_id as " + SFV_PREFIX + "study_form_variables_id, " +
                "StudyFormVariables.temp_master_study_forms_id as " + SFV_PREFIX + "temp_master_study_forms_id, " +
                "StudyFormVariables.master_variables_id as " + SFV_PREFIX + "master_variables_id, " +
                "StudyFormVariables.created_on as " + SFV_PREFIX + "created_on, " +
                "StudyFormVariables.last_modified as " + SFV_PREFIX + "last_modified, " +
                "StudyFormVariables.timezone as " + SFV_PREFIX + "timezone, " +
                "StudyFormVariables.is_active as " + SFV_PREFIX + "is_active, " +
                "StudyFormVariables.is_server_updated as " + SFV_PREFIX + "is_server_updated " +
                "FROM MasterVariables " +
                "Inner Join StudyFormVariables on StudyFormVariables.master_variables_id = MasterVariables.master_variables_id " +
                "AND StudyFormVariables.temp_master_study_forms_id = :formId " +
                "WHERE StudyFormVariables.temp_study_form_variables_id IN(:tempFormVariableIDs) " +
                "AND MasterVariables.when_asked IN(:studyType) " +
                "order by MasterVariables.variable_category Asc"
    )
    fun getStudyFormWithDetailVariable(
        tempFormVariableIDs: List<String>,
        formId: String, studyType: List<Int>
    ): Single<List<StudyFormWithVariable>>

    @Transaction
    @Query(
        "SELECT StudyFormVariables.temp_study_form_variables_id " +
                "FROM StudyFormVariables " +
                "INNER JOIN MasterVariables on StudyFormVariables.master_variables_id = MasterVariables.master_variables_id " +
                "AND StudyFormVariables.temp_master_study_forms_id = :formId " +
                "AND MasterVariables.is_searchable = 1 "
    )
    fun getSearchableFormVariables(
        formId: String
    ): Single<List<String>>

    @Query("DELETE FROM StudyFormVariables")
    fun deleteAllVariable(): Int

    @Parcelize
    data class StudyFormWithVariable(
        @Embedded val masterVariables: MasterVariables,

        @Embedded(prefix = SFV_PREFIX)
        val formVariables: StudyFormVariables

    ) : Parcelable

}