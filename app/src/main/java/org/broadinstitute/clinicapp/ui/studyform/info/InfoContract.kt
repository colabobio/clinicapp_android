package org.broadinstitute.clinicapp.ui.studyform.info

import org.broadinstitute.clinicapp.base.BaseContract


class InfoContract {

    interface View : BaseContract.View {
        fun isDuplicate(isDuplicate: Boolean)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun isDuplicateTitle(formTitle: String)
    }
}