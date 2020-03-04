package org.broadinstitute.clinicapp.data.source.local.dao

import android.os.Parcelable
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.broadinstitute.clinicapp.data.source.local.entities.SyncStatus
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize


@Dao
interface SyncStatusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSyncStatus(syncStatus: SyncStatus): Long

    @Query("Delete FROM SyncStatus")
    fun deleteAll(): Int

    @Query(
        "Select * from (select MAX(last_sync_time) as lastModifiedForms from SyncStatus where SyncStatus.table_name=\"MasterStudyForms\"), " +
                "(select MAX(last_sync_time)  as lastModifiedStudies from SyncStatus where SyncStatus.table_name=\"MasterStudyData\"), " +
                "(select MAX (last_sync_time) as lastModifiedVariable from SyncStatus where SyncStatus.table_name=\"MasterVariables\")"
    )
    fun getAllLastModified(): Single<AllLastSyncDate>

    @Parcelize
    data class AllLastSyncDate(
        var lastModifiedForms: Long,
        var lastModifiedStudies: Long,
        var lastModifiedVariable: Long

    ) : Parcelable
}