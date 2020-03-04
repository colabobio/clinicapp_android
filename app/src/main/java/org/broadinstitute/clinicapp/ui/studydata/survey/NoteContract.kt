package org.broadinstitute.clinicapp.ui.studydata.survey

import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyData


class NoteContract {

    interface View : BaseContract.View {
        fun studyDataInserted()
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun insertStudyData(
            patient: Patient,
            masterStudyData: MasterStudyData,
            studyDataList: List<StudyData>
        )
    }
}