package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import androidx.room.ForeignKey.NO_ACTION
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Entity(
    foreignKeys = [ForeignKey(
        entity = MasterVariables::class,
        parentColumns = arrayOf("master_variables_id"),
        childColumns = arrayOf("master_variables_id"),
        onUpdate = NO_ACTION
    ), ForeignKey(
        entity = MasterStudyForms::class,
        parentColumns = arrayOf("temp_master_study_forms_id"),
        childColumns = arrayOf("temp_master_study_forms_id"),
        onUpdate = NO_ACTION
    )],
    indices = [Index("master_variables_id"), Index("temp_master_study_forms_id"), Index(
        "temp_study_form_variables_id",
        unique = true
    )]
)
@Parcelize
data class StudyFormVariables(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "study_form_variables_id") var id: Long = 0L,

    @Expose
    @ColumnInfo(name = "temp_study_form_variables_id")
    var tempStudyFormVariablesId: String = "",

    @ColumnInfo(name = "temp_master_study_forms_id")
    var masterStudyFormsIdFk: String = "",

    @Expose
    @ColumnInfo(name = "master_variables_id") var masterVariablesIdFk: Long = 0L,

    @Expose
    @ColumnInfo(name = "created_on") var createdOn: Long = System.currentTimeMillis(),

    @Expose
    @ColumnInfo(name = "last_modified") var lastModified: Long = System.currentTimeMillis(),

    @Expose
    var timezone: String = "",

    @Expose
    @ColumnInfo(name = "is_active") var isActive: Boolean = true,

    @ColumnInfo(name = "is_server_updated")
    var isServerUpdated: Boolean = false
) : Parcelable