package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize

@Entity(
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = arrayOf("admin_id"),
        childColumns = arrayOf("admin_id"),
        onUpdate = ForeignKey.NO_ACTION

    ), ForeignKey(
        entity = MasterStudyForms::class,
        parentColumns = arrayOf("temp_master_study_forms_id"),
        childColumns = arrayOf("temp_master_study_forms_id"),
        onUpdate = ForeignKey.NO_ACTION


    )],
    indices = [Index("temp_master_study_forms_id"), Index("admin_id"),
        Index("temp_master_study_data_id", unique = true)]
)
@Parcelize
data class MasterStudyData(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "master_study_data_id")
    var id: Long = 0L,

    @Expose
    @ColumnInfo(name = "temp_master_study_data_id")
    var tempMasterStudyDataId: String = "",

    @Expose
    @ColumnInfo(name = "temp_master_study_forms_id")
    var tempMasterStudyFormsId: String = "",

    @Ignore
    @Expose
    var studyDatas: List<StudyData> = arrayListOf(),

    @Ignore
    @Expose
    var patient: Patient? = null,

    @Expose
    var latitude: Double = 0.0,

    @Expose
    var longitude: Double = 0.0,

    @Expose
    @ColumnInfo(name = "user_id")
    var keycloakUserFk: String = "",

    @ColumnInfo(name = "admin_id")
    var adminId: String = "",

    @Expose
    @ColumnInfo(name = "created_on")
    var createdOn: Long = System.currentTimeMillis(),

    @Expose
    @ColumnInfo(name = "last_modified")
    var lastModified: Long = System.currentTimeMillis(),

    @Expose
    @ColumnInfo(name = "timezone")
    var timezone: String = "",

    @Expose
    var note: String = "",

    @Expose
    var version: String = "",

    @Expose
    @ColumnInfo(name = "study_data_when_asked")
    var studyDataWhenAsked: Int = 0,

    @ColumnInfo(name = "is_server_updated")
    var isServerUpdated: Boolean = false
) : Parcelable