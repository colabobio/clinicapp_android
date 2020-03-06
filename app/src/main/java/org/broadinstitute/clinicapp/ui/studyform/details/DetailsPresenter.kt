package org.broadinstitute.clinicapp.ui.studyform.details

import android.content.Context
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormVariables

class DetailsPresenter(private var view: DetailsContract.View, val context: Context) :
    DetailsContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    override fun importStudyForm(userID: String, masterStudyForms: MasterStudyForms) {
        view.showProgress(true)
        compositeDisposable.add(repository.associateStudyForm(
            userID,
            masterStudyForms.tempMasterStudyFormsId.toString()
        )
            .doOnSuccess { data ->

                if (data.message?.contains("Association already exists", true)!!) {
                    view.showSnackBarMessage(context.getString(R.string.already_import_error))
                } else {
                    view.successImportStudy()
                    masterStudyForms.isServerUpdated = true
                    repository.insertMasterStudyForm(masterStudyForms)
                    masterStudyForms.studyFormVariables.forEach { variable ->
                        variable.isServerUpdated = true
                        variable.masterStudyFormsIdFk = masterStudyForms.tempMasterStudyFormsId!!
                    }
                    repository.insertStudyFormVariables(masterStudyForms.studyFormVariables)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                view.showProgress(false)


            },
                { throwable ->
                    view.showProgress(false)
                    // Handle error of Auth
                    view.handleThrowable(throwable)
                   // view.showSnackBarMessage(throwable.localizedMessage!!)
                }
            )
        )

    }

    override fun getMasterVariables(studyFromID: String) {

        compositeDisposable.add(repository.getFormVariables(studyFromID, arrayListOf(0, 1, 2))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                view.showVariables(data)
            },
                { it.printStackTrace() }
            )
        )

    }

    private fun getVariableIDObservable(variables: List<StudyFormVariables>): Observable<StudyFormVariables> {

        return Observable
            .create(ObservableOnSubscribe<StudyFormVariables> { emitter ->
                for (category in variables) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(category)
                    }
                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    private fun getMasterVariable(masterVariableID: Long): Observable<MasterVariables> {

        return repository.getMasterVariableById(masterVariableID).toObservable()
    }

    fun getMasterVariables(studyFormDetail: StudyFormDetail) {

        val list = arrayListOf<StudyFormVariablesDao.StudyFormWithVariable>()

        getVariableIDObservable(studyFormDetail.variables)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.computation())
            .flatMap { category ->
                getMasterVariable(category.masterVariablesIdFk)
                    .map { t ->
                        return@map StudyFormVariablesDao.StudyFormWithVariable(t, category)
                    }

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<StudyFormVariablesDao.StudyFormWithVariable> {
                override fun onNext(variables: StudyFormVariablesDao.StudyFormWithVariable) {
                    if (variables.formVariables.isActive) {
                        list.add(variables)
                    }
                }

                override fun onComplete() {
                    view.showVariables(list)
                }

                override fun onError(e: Throwable) {

                }

                override fun onSubscribe(d: Disposable) {
                    //  disposable = d
                }

            })
    }

    override fun isDuplicateTitle(formTitle: String) {
        compositeDisposable.add(
            repository.checkFormTitle(formTitle)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ titles ->
                    view.isDuplicate(!titles.isNullOrEmpty())
                },
                    { throwable -> throwable.printStackTrace() }
                )
        )
    }


    override fun unsubscribe() {
        compositeDisposable.clear()

    }

    override fun attach(view: DetailsContract.View) {
        this.view = view
    }

}