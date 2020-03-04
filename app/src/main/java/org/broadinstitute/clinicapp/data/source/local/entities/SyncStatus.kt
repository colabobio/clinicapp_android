package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(indices = [Index(value = arrayOf("table_name"), unique = true)])
@Parcelize
data class SyncStatus (
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Long = 0L,
    @ColumnInfo(name = "table_name")
    var tableName: String = "",
    @ColumnInfo(name = "last_sync_time")
    var lastSyncTime: Long = 0L
) : Parcelable