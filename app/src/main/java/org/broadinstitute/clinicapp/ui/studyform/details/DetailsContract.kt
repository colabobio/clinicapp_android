package org.broadinstitute.clinicapp.ui.studyform.details

import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms

interface DetailsContract {

    interface View : BaseContract.View {
        fun showVariables(list: List<StudyFormVariablesDao.StudyFormWithVariable>)

        fun showConfirmOption()

        fun showConfirmWithAdd()

        fun successImportStudy()
        fun isDuplicate(b: Boolean)

        fun handleThrowable(throwable: Throwable)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun getMasterVariables(studyFromID: String)

        fun importStudyForm( userID : String, masterStudyForms : MasterStudyForms)
        fun isDuplicateTitle(formTitle: String)
    }
}