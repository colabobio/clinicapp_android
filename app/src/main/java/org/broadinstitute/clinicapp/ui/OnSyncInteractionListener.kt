package org.broadinstitute.clinicapp.ui

import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail

interface OnSyncInteractionListener {

        fun onSyncClick(item: StudyFormDetail)
    }