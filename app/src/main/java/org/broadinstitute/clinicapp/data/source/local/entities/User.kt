package org.broadinstitute.clinicapp.data.source.local.entities

import com.google.gson.annotations.Expose

data class User(

    @Expose var keycloakUser: String = "",
    @Expose(serialize = false, deserialize = true) var id: Long = 0L,
    @Expose  var firstName: String = "",
    @Expose var lastName: String = "",
    @Expose  var emailId: String = "",
    @Expose  var workLocation: String = "",
    @Expose  var gender: String = ""
)