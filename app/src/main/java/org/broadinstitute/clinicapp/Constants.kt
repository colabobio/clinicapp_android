package org.broadinstitute.clinicapp

object Constants {
    const val API_ENABLED = false
    const val LOC_ENABLED = false
    const val DRV_ENABLED = true

    const val LIMIT = 20
    const val MASTER_VARIABLES_LIMIT = 100
    const val STUDY_DATA_LIMIT = 20

    object PrefKey {
        const val PREF_USER_NAME = "userName"
        const val PREF_USER_ID = "USER_ID"
        const val PREF_EMAIL = "emailId"
        const val PREF_FIRST_NAME = "firstName"
        const val PREF_LAST_NAME = "lastName"
        const val PREF_LATITUDE = "latitude"
        const val PREF_LONGITUDE = "longitude"
        const val PREF_ACCESS_TOKEN = "token"
        const val PREF_GENDER = "userGender"
        const val PREF_WORK_LOCATION = "userWorkLocation"
        // First login - when user created its values is true, after logout user tries login with same credentials
        // check condition for API hit of create user : check pref ID and response user ID keycloak if same then
        // API should not call, if different then call API of create new user and set new data pref and clear data in DB.
        const val PREF_USER_CREATED = "user_created"

        const val PREF_ACCESS_TOKEN_EXPIRES = "PREF_ACCESS_TOKEN_EXPIRES"
        const val PREF_INITIAL_LAUNCH = "initial_launch_for_master_Variable"
    }

    object TempIdType {
        const val MASTER_STUDY_FORM_TEMP = "MSF"
        const val STUDY_FORM_VARIABLE_TEMP = "SFV"
        const val MASTER_STUDY_DATA_TEMP = "MSD"
        const val STUDY_DATA_TEMP = "SD"
    }

    object BundleKey {

        //Home activity
        const val HOME_ACTIVITY_KEY = "HOME_SCREEN_CALL_FROM"
        const val HOME_CALL_FROM_LOGIN = "HOME_SCREEN_CALL_FROM_LOGIN"
        const val HOME_CALL_FROM_SPLASH = "HOME_SCREEN_CALL_FROM_SPLASH"


        const val STUDY_FORM_DETAIL_KEY = "study_form_detail"
        const val PATIENT_KEY = "patient_key"

        const val CALL_DETAILS_STUDY_FORM_KEY = "call_details_from_activity_value"

        const val CREATE_STUDY_FORM_KEY = "create_study_form"
        const val EXISTING_STUDY_FORM_KEY = "existing_study_form"


        const val CREATE_STUDY_DATA_KEY = "create_study_data_key"
        const val MASTER_STUDY_DATA_KEY = "master_study_DATA"
    }

    object CallingPageValue {
        const val HOME_EDIT_STUDY_FORM = "HOME_EDIT_STUDY_FORM"
        const val CREATE_FROM_TEMPLATE_STUDY_FORM = "CREATE_FROM_TEMPLATE_STUDY_FORM"
        const val IMPORT_FROM_ONLINE_STUDY_FORM = "IMPORT_FROM_ONLINE_STUDY_FORM"
        const val CREATE_FROM_SCRATCH_STUDY_FORM = "CREATE_FROM_SCRATCH_STUDY_FORM"
    }

    object StudyDataType {
        const val NEW_PATIENT_STUDY_DATA = 0
        const val FOLLOWUP_STUDY_DATA = 1
        const val FINAL_OUTCOME_STUDY_DATA = 2
        const val UPDATE_STUDY_DATA = 3
    }
}