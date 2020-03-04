package org.broadinstitute.clinicapp.ui.studydata.survey

import android.content.Context
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyData


class NotePresenter(private var view: NoteContract.View, val context: Context) :
    NoteContract.Presenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository =
        ClinicRepository.getInstance(context)

    override fun insertStudyData(
        patient: Patient,
        masterStudyData: MasterStudyData,
        studyDataList: List<StudyData>
    ) {
        compositeDisposable.add(repository.insertPatientSingle(patient)
            .doOnSuccess {
                repository.insertMasterStudyData(masterStudyData)
                repository.insertStudyData(studyDataList)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.showProgress(false)
                view.studyDataInserted()
            },

                { throwable ->
                    throwable.printStackTrace()
                }

            ))
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun attach(view: NoteContract.View) {
        this.view = view
    }
}