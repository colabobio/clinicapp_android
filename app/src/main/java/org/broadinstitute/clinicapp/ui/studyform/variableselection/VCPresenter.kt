package org.broadinstitute.clinicapp.ui.studyform.variableselection

import android.content.Context
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyForms
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormVariables


class VCPresenter(private var view: VCContract.View, val context: Context) : VCContract.Presenter {
    private var disposable: Disposable? = null
    val source = PublishSubject.create<ArrayList<List<MasterVariables>>>()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository =
        ClinicRepository.getInstance(context)

    override fun getCategories(limit: Int, offset: Int) {
        val variablesList = HashMap<String, List<MasterVariables>>()
        val catList = ArrayList<String>()
        compositeDisposable.add(repository.getCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ categories ->
                getCategoriesObservable(categories)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .flatMap { category -> getMVListObservable(category) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<List<MasterVariables>> {
                        override fun onNext(variables: List<MasterVariables>) {
                            variablesList[variables[0].variableCategory] = variables
                            catList.add(variables[0].variableCategory)
                        }

                        override fun onComplete() {

                            view.showMasterVariables(variablesList, catList)
                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onSubscribe(d: Disposable) {
                            disposable = d
                        }

                    })
            },
                { throwable ->

                    throwable.printStackTrace()
                }

            ))
    }

    private fun getMVListObservable(category: String): Observable<List<MasterVariables>> {

        return Observable
            .create(ObservableOnSubscribe<List<MasterVariables>> { emitter ->
                val mvList = repository.getMasterVariablesByCategory(category)
                if (!emitter.isDisposed) {
                    emitter.onNext(mvList)
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getCategoriesObservable(categories: List<String>): Observable<String> {

        return Observable
            .create(ObservableOnSubscribe<String> { emitter ->
                for (category in categories) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(category)
                    }
                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    override fun insertMasterStudyForm(
        masterStudyForms: MasterStudyForms,
        studyFormVariablesList: List<StudyFormVariables>
    ) {
        compositeDisposable.add(repository.insertSingleStudyForm(masterStudyForms)
            .doOnSuccess {
                studyFormVariablesList.forEach { variable ->
                    variable.masterStudyFormsIdFk = masterStudyForms.tempMasterStudyFormsId!!
                }
             repository.insertStudyFormVariables(studyFormVariablesList)
            }

            .subscribeOn(Schedulers.io())

            .observeOn(AndroidSchedulers.mainThread())

            .subscribe({
                view.showProgress(false)
            },

                { throwable -> throwable.printStackTrace() }

            ))
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun attach(view: VCContract.View) {
        this.view = view
    }
}