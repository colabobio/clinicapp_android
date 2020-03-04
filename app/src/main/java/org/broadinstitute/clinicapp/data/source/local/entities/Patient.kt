package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Entity(indices = [Index("admin_id", unique = true)])
@Parcelize
data class Patient(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "patient_id")
    var id: Long = 0L,

    @Expose
    @ColumnInfo(name = "admin_id")
    var adminId: String = "",

    @Expose
    var demographics: String = ""
) : Parcelable