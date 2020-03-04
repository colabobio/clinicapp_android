package org.broadinstitute.clinicapp.ui.studyform

import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail

interface ItemContract {

    interface View : BaseContract.View {
        fun showStudyForm(list: ArrayList<StudyFormDetail>)
        fun handleThrowable( throwable: Throwable)

    }

    interface Presenter : BaseContract.Presenter<View> {
        fun getSearchedForms(query: String, offset: Int)

        fun getSearchedFormsOnline(query: String, offset: Int, showProgress : Boolean, userD : String?)

        fun loadMoreStudyForms(fromScreen: String, searchCriteria: String, isNetwork: Boolean, userID : String?)

    }
}