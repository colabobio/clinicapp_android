package org.broadinstitute.clinicapp.ui.home

interface HomeFragmentContract {

    interface View {
        fun getMyStudiesToFragment(query: String)
        fun getMyModelsToFragment()
    }


}