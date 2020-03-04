package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables

class MasterVariablesResponse : GenericResponse() {
    @Expose
    var data: List<MasterVariables> = arrayListOf()
}