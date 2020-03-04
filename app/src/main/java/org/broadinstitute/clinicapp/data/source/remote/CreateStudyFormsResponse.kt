package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose

class CreateStudyFormsResponse : GenericResponse() {
    @Expose
    var success: List<String>? = null

    @Expose
    var failed: List<String>? = null
}