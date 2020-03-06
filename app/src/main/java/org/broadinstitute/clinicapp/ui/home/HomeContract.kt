package org.broadinstitute.clinicapp.ui.home

import androidx.paging.PagedList
import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail


class HomeContract {

    interface View : BaseContract.View {
        fun showStudyForms(list: PagedList<StudyFormDetail>)

        fun updateProgress(show: Boolean, syncMessage: String)

        fun handleLogout(message: String, positiveOption: String, negativeOption: String)

        fun showEmptyWarning(isEmpty: Boolean)
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun checkMasterVariables(offset: Int)

        fun getMasterVariables(pageNo: Int, lastModified: Long, isInitialLoad: Boolean)

        fun getStudyFormsFromDB(search: String)

       // fun getUnSyncedForms()

        fun getMyStudyFormsFromAPI(isSingleCall: Boolean)

        fun checkStudyFormsInDB(isSingleCall: Boolean)
        fun checkUnSyncData()

        fun syncIndividualForm(formDetail: StudyFormDetail)

    }
}