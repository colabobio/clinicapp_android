package org.broadinstitute.clinicapp.ui.studyform.variableselection

import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables

interface OnInfoSelectedListener {

    fun onInfoSelected(masterVariables: MasterVariables)
}