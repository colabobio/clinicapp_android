package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class GenericResponse {

    @Expose
    @SerializedName("status_code")
    var statusCode: String = ""

    @Expose
    var message: String? = null

    @Expose
    @SerializedName("total_count")
    var totalCount: Int = 0

    @Expose
    @SerializedName("error_code")
    var errorCode: String = ""
}