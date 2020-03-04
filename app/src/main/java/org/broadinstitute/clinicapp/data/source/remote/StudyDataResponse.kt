package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData

class StudyDataResponse : GenericResponse() {
    @Expose
    var data: List<MasterStudyData>? = null
}