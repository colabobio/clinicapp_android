package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms

class StudyFormsResponse : GenericResponse() {
    @Expose
    var data: List<MasterStudyForms>? = null
}