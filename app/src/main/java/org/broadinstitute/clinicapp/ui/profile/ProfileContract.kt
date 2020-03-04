package org.broadinstitute.clinicapp.ui.profile

import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.User


class ProfileContract {

    interface View : BaseContract.View {
        fun userUpdated(user: User)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun updateUser(user: User)
    }
}