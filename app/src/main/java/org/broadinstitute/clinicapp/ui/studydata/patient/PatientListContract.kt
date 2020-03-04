package org.broadinstitute.clinicapp.ui.studydata.patient

import androidx.paging.PagedList
import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.Patient


class PatientListContract {

    interface View : BaseContract.View {
        fun showPatients(patientList: PagedList<Patient>)

        fun showEmptyWarning(isEmpty: Boolean)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun getPatients(search: String, studyFormId: String)
    }
}