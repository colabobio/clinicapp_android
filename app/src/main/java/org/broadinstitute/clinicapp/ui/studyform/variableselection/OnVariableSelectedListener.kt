package org.broadinstitute.clinicapp.ui.studyform.variableselection

import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables

interface OnVariableSelectedListener {

    fun onVariableSelected(masterVariables: MasterVariables, isSelected: Boolean)
}