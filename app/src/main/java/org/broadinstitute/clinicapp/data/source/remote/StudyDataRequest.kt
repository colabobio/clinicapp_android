package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import org.broadinstitute.clinicapp.data.source.local.entities.StudyDataWithPatient

class StudyDataRequest {
    @Expose
    var studyDataRequests: List<StudyDataWithPatient>? = null
}