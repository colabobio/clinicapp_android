package org.broadinstitute.clinicapp.ui.profile

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_profile.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.User

class ProfileActivity : BaseActivity(), ProfileContract.View {
    private lateinit var presenter: ProfilePresenter
    private var gender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, this)

        setUp()
    }

    override fun setUp() {
        edtUserName.setText(pref.readStringFromPref(Constants.PrefKey.PREF_USER_NAME))

        edtUserFirstName.setText(pref.readStringFromPref(Constants.PrefKey.PREF_FIRST_NAME))

        edtUserLastName.setText(pref.readStringFromPref(Constants.PrefKey.PREF_LAST_NAME))

        edtUserEmail.setText(pref.readStringFromPref(Constants.PrefKey.PREF_EMAIL))

        edtUserWorkLocation.setText(pref.readStringFromPref(Constants.PrefKey.PREF_WORK_LOCATION))

        rgGender.setOnCheckedChangeListener { _, checkedId ->
            gender = when (checkedId) {
                R.id.rbFemale -> getString(R.string.female)
                R.id.rbMale -> getString(R.string.male)
                else -> getString(R.string.others)
            }
        }

        when {
            pref.readStringFromPref(Constants.PrefKey.PREF_GENDER) == getString(R.string.female) -> rbFemale.isChecked =
                true
            pref.readStringFromPref(Constants.PrefKey.PREF_GENDER) == getString(R.string.male) -> rbMale.isChecked =
                true
            pref.readStringFromPref(Constants.PrefKey.PREF_GENDER) == getString(R.string.others) -> rbOthers.isChecked =
                true
        }

        btnUpdate.setOnClickListener {
            val fName = edtUserFirstName.text
            val lName = edtUserLastName.text
            val email = edtUserEmail.text
            val workLocation = edtUserWorkLocation.text

            val user = User(
                firstName = fName.toString(),
                lastName = lName.toString(),
                emailId = email.toString(),
                gender = gender,
                workLocation = workLocation.toString()
            )
            presenter.updateUser(user)
        }
    }

    override fun userUpdated(user: User) {
        pref.writeStringToPref(Constants.PrefKey.PREF_FIRST_NAME, user.firstName)
        pref.writeStringToPref(Constants.PrefKey.PREF_LAST_NAME, user.lastName)
        pref.writeStringToPref(Constants.PrefKey.PREF_EMAIL, user.emailId)
        pref.writeLongToPref(Constants.PrefKey.PREF_USER_ID, user.id)
        pref.writeStringToPref(Constants.PrefKey.PREF_GENDER, user.gender)
        pref.writeStringToPref(Constants.PrefKey.PREF_WORK_LOCATION, user.workLocation)
        finish()
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else {
            hideLoading()
        }

    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }
}
