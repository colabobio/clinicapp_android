package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StudyFormDetail(
    @Embedded val masterStudyForms: MasterStudyForms,
    @Relation(
        parentColumn = "temp_master_study_forms_id",
        entityColumn = "temp_master_study_forms_id"
    )
    val variables: List<StudyFormVariables>
) : Parcelable
