package org.broadinstitute.clinicapp.ui.login

import org.broadinstitute.clinicapp.base.BaseContract


class LoginContract {

    interface View : BaseContract.View {
        fun handleAccessSuccess()
        fun handleUserCreation()
        fun failedUserCreation()
        fun handleDifferentUserLogin(username :String, email:String, firstName:String,  lastName:String, token :String)
        fun reloadLoginPage()

        fun deletedData(username :String, email:String, firstName:String,  lastName:String, token :String)
    }

    interface Presenter: BaseContract.Presenter<View> {
        fun grantNewAccessToken(code :String, clientId:String, redirectUri:String, showLoadingUI: Boolean)
        fun createNewUser(username :String, email:String, firstName:String,  lastName:String, token :String)
        fun deleteDataOfPreviousUser(
            username: String,
            email: String,
            firstName: String,
            lastName: String,
            token: String
        )
        fun logoutUser(clientId: String)
    }
}