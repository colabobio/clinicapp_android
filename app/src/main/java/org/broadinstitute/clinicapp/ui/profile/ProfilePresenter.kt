package org.broadinstitute.clinicapp.ui.profile

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.User
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils


class ProfilePresenter(private var view: ProfileContract.View, val context: Context) :
    ProfileContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    override fun updateUser(user: User) {
        if (NetworkUtils.isNetworkConnected(context)) {
             view.showProgress(true)
            compositeDisposable.add(repository.updateUser(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showProgress(false)
                    view.userUpdated(it.userDetailsdto!!)
                },
                    { throwable ->
                        view.showProgress(false)
                        view.showSnackBarMessage(CommonUtils.getErrorMessage(throwable))
                    }
                )

            )

        }
    }


    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun attach(view: ProfileContract.View) {
        this.view = view
    }

}