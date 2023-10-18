package org.broadinstitute.clinicapp.ui.studydata.patient

import android.content.Context
import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import java.util.*


class PatientListPresenter(private var view: PatientListContract.View, val context: Context) :
    PatientListContract.Presenter {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    override fun getPatients(search: String, studyFormId: String) {
//        Log.d("PATIENT LIST SEARCH QUERY", search)
        view.showProgress(true)
        compositeDisposable.add(getPatientObservable(search, studyFormId).subscribe({
                patients ->
            view.showProgress(false)
            if (patients.isNotEmpty()) {
                view.showPatients(patients)
//                Log.d("PATIENT LIST", patients.toString())
            }else{
                if(search.isEmpty())view.showEmptyWarning(true)
                else {
                    view.showToastMessage(context.getString(R.string.no_result_found))
                }
            }
        },
            {
                view.showProgress(false)
            }
        )
        )
    }

    private fun getPatientObservable(
        search: String,
        studyFormId: String
    ): Flowable<PagedList<Patient>> {
        val pageSize = Constants.LIMIT

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(pageSize)
            .setPageSize(pageSize).build()

        val factory: DataSource.Factory<Int, Patient> = repository.getPatients(search.toLowerCase(
            Locale.ENGLISH), studyFormId)

        val pagedListBuilder: RxPagedListBuilder<Int, Patient> =
            RxPagedListBuilder(
                factory,
                pagedListConfig
            )

        pagedListBuilder.setFetchScheduler(Schedulers.io())
        pagedListBuilder.setNotifyScheduler(AndroidSchedulers.mainThread())
        return pagedListBuilder.buildFlowable(BackpressureStrategy.LATEST)

    }


    override fun unsubscribe() {
        compositeDisposable.clear()
    }

    override fun attach(view: PatientListContract.View) {
        this.view = view
    }
}