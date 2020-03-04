package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StudyDataWithPatient(
    @Embedded
    @Expose
    val patient: Patient,
    @Relation(
        parentColumn = "admin_id",
        entityColumn = "admin_id"

    )
    @Expose
    val masterStudyDataList: List<MasterStudyData>
) : Parcelable

