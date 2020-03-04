package org.broadinstitute.clinicapp.data.source.remote

import com.google.gson.annotations.Expose
import org.broadinstitute.clinicapp.data.source.local.entities.User

class UserResponse : GenericResponse() {

    @Expose
    var userDetailsdto: User? = null
}