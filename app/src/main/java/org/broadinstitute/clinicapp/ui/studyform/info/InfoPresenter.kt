package org.broadinstitute.clinicapp.ui.studyform.info

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.data.source.ClinicRepository


class InfoPresenter(private var view: InfoContract.View, val context: Context) :
    InfoContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    override fun isDuplicateTitle(formTitle: String) {
        compositeDisposable.add(
            repository.checkFormTitle(formTitle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { titles ->
                    view.isDuplicate(!titles.isNullOrEmpty())
                }
        )
    }


    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun attach(view: InfoContract.View) {
        this.view = view
    }

}