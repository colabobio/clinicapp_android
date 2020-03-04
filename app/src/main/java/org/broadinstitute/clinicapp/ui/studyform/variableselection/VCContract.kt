package org.broadinstitute.clinicapp.ui.studyform.variableselection

import org.broadinstitute.clinicapp.base.BaseContract
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormVariables


class VCContract {

    interface View : BaseContract.View {
        fun showMasterVariables(
            mvMap: HashMap<String, List<MasterVariables>>,
            catList: ArrayList<String>
        )
    }

    interface Presenter : BaseContract.Presenter<View> {
        fun getCategories(limit: Int, offset: Int)

        fun insertMasterStudyForm(
            masterStudyForms: MasterStudyForms,
            studyFormVariablesList: List<StudyFormVariables>
        )
    }
}