package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Entity(
    foreignKeys = [ForeignKey(
        entity = MasterStudyData::class,
        parentColumns = arrayOf("temp_master_study_data_id"),
        childColumns = arrayOf("temp_master_study_data_id"),
        onUpdate = ForeignKey.NO_ACTION

    ), ForeignKey(
        entity = StudyFormVariables::class,
        parentColumns = arrayOf("temp_study_form_variables_id"),
        childColumns = arrayOf("temp_study_form_variables_id"),
        onUpdate = ForeignKey.NO_ACTION
    )],
    indices = [Index("temp_master_study_data_id"), Index("temp_study_form_variables_id"),
        Index("temp_study_data_id", unique = true), Index("variable_value")]
)
@Parcelize
data class StudyData(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "study_data_id")
    var id: Long = 0L,

    @Expose
    @ColumnInfo(name = "temp_study_data_id")
    var tempStudyDataId: String = "",

    @ColumnInfo(name = "temp_master_study_data_id")
    var tempMasterStudyDataId: String = "",

    @Expose
    @ColumnInfo(name = "temp_study_form_variables_id")
    var tempStudyFormVariablesId: String = "",

    @Expose
    @ColumnInfo(name = "captured_on")
    var capturedOn: Long = System.currentTimeMillis(),

    @Expose
    @ColumnInfo(name = "variable_value")
    var variableValue: String = "",

    @Expose
    @ColumnInfo(name = "timezone")
    var timezone: String = "",

    @Expose
    @ColumnInfo(name = "last_modified")
    var lastModified: Long = System.currentTimeMillis()

) : Parcelable