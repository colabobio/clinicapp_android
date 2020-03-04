package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Entity(indices = [Index("temp_master_study_forms_id", unique = true)])
@Parcelize
data class MasterStudyForms(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "master_study_forms_id") var id: Long = 0L,

    @Expose
    @ColumnInfo(name = "temp_master_study_forms_id")
    var tempMasterStudyFormsId: String? = "",

    @Ignore
    @Expose
    @SerializedName("studyFormVars")
    var studyFormVariables: List<StudyFormVariables> = arrayListOf(),

    @Expose
    var title: String = "",

    @Expose
    var description: String = "",

    @Expose
    @SerializedName("keycloakUserFk")
    @ColumnInfo(name = "user_id") var userId: String = "",

    @Expose(serialize = false, deserialize = true)
    @ColumnInfo(name = "creator") var creator: String = "",

    @Expose
    @ColumnInfo(name = "created_on") var createdOn: Long = System.currentTimeMillis(),

    @Expose
    @ColumnInfo(name = "last_modified") var lastModified: Long = System.currentTimeMillis(),

    @Expose
    var timezone: String = "",

    @Expose
    var version: String = "",

    @Expose
    @ColumnInfo(name = "is_active") var isActive: Boolean = true,

    @ColumnInfo(name = "is_server_updated")
    var isServerUpdated: Boolean = false,

    @Ignore
    var isFromOffline: Boolean = true
) : Parcelable