package org.broadinstitute.clinicapp.data.source.local.entities

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(indices = [Index("variable_name", unique = true)])
@Parcelize
data class MasterVariables(
    @PrimaryKey
    @NonNull
    @Expose
    @ColumnInfo(name = "master_variables_id") var id: Long = 0L,
    @Expose
    @ColumnInfo(name = "variable_category") var variableCategory: String = "",
    @Expose
    @ColumnInfo(name = "variable_name") var variableName: String = "",
    @Expose
    var label: String = "",
    @Expose
    var description: String = "",
    @Expose
    var type: String = "",
    @Expose
    @ColumnInfo(name = "when_asked") var whenAsked: Int = 0,
    @Expose
    @ColumnInfo(name = "binary_options") var binaryOptions: Int = 0,
    @Expose
    @ColumnInfo(name = "categorical_opt1") var categoricalOpt1: String = "",
    @Expose
    @ColumnInfo(name = "categorical_opt2") var categoricalOpt2: String = "",
    @Expose
    @ColumnInfo(name = "categorical_opt3") var categoricalOpt3: String = "",
    @Expose
    @ColumnInfo(name = "categorical_opt4") var categoricalOpt4: String = "",
    @Expose
    @ColumnInfo(name = "categorical_opt5") var categoricalOpt5: String = "",
    @Expose
    @ColumnInfo(name = "created_on") var createdOn: Long? = Calendar.getInstance().timeInMillis,
    @Expose
    @ColumnInfo(name = "last_modified") var lastModified: Long? = Calendar.getInstance().timeInMillis,
    @Expose
    @ColumnInfo(name = "last_modified_by") var keycloakUserFk: String = "",
    @Expose
    var timezone: String = "",
    @Expose
    var version: Int = 0,
    @Expose
    @ColumnInfo(name = "is_active") var isActive: Boolean = true,
    @Expose
    @ColumnInfo(name = "is_multi_select") var isMultiSelect: Boolean = false,
    @Expose
    @ColumnInfo(name = "is_searchable") var isSearchable: Boolean = false,
    @Expose
    @ColumnInfo(name = "is_mandatory") var isMandatory: Boolean = false
) : Parcelable