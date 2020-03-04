package org.broadinstitute.clinicapp.base

interface BaseContract {

    interface Presenter<in T> {

        fun unsubscribe()
        fun attach(view: T)
    }

    interface View {
        fun showProgress(show: Boolean)
        fun showSnackBarMessage(message: String)
        fun showToastMessage(message: String)
    }

}