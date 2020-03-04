package org.broadinstitute.clinicapp.data.source.local.dao

import android.os.Parcelable
import androidx.paging.DataSource
import androidx.room.*
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize


@Dao
interface MasterStudyFormsDao {
    /**
     * Insert a list in the database. If the item already exists, replace it.
     *
     * @param masterStudyForms to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insert(masterStudyForms: MasterStudyForms): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertSingle(masterStudyForms: MasterStudyForms): Single<Long>

    @Query("UPDATE MasterStudyForms SET is_server_updated = 1 WHERE temp_master_study_forms_id = :tempId AND is_active = 1")
    fun updateMasterStudyForm(tempId: String): Int

    @Transaction
    @Query("SELECT * FROM MasterStudyForms WHERE is_active = 1 ORDER BY last_modified DESC LIMIT :limit OFFSET :offset")
    fun getAllMasterStudyForms(limit: Int, offset: Int): Flowable<List<StudyFormDetail>>

    @Query("SELECT * FROM MasterStudyForms WHERE is_active = 1 AND is_server_updated = 0")
    fun getUnSyncedStudyForms(): Single<List<MasterStudyForms>>

    @Transaction
    @Query("SELECT * FROM MasterStudyForms WHERE title LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%' ORDER BY last_modified DESC LIMIT :limit OFFSET :offset")
    fun searchStudyForms(search: String?, limit: Int, offset: Int): Flowable<List<StudyFormDetail>>

    @Transaction
    @Query("SELECT Count(*) FROM MasterStudyForms WHERE title LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%'")
    fun searchStudyFormsCount(search: String?): Single<Int>

    @Transaction
    @Query("SELECT * FROM MasterStudyForms WHERE is_active = 1 AND title LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%' ORDER BY last_modified DESC")
    fun searchStudyFormsItems(search: String?): DataSource.Factory<Int, StudyFormDetail>

    @Query("SELECT * FROM MasterStudyForms WHERE title LIKE :formTitle")
    fun checkTitle(formTitle: String): Single<List<MasterStudyForms>>

    @Query("SELECT count(*) FROM MasterStudyForms")
    fun getCount(): Single<Int>

    @Query("DELETE FROM MasterStudyForms")
    fun deleteStudyForms(): Int

    @Query("SELECT MAX(last_modified) FROM MasterStudyForms")
    fun getLastModified(): Long


    @Query(
        "SELECT * from (SELECT count(*) as formCount FROM MasterStudyForms WHERE is_active = 1 AND is_server_updated = 0), " +
                "(SELECT count(*) as dataCount FROM MasterStudyData WHERE is_server_updated = 0)"
    )
    fun getUnSyncCounts(): Single<UnSyncCount>

    @Parcelize
    data class UnSyncCount(
        var formCount: Long,
        var dataCount: Long

    ) : Parcelable
}