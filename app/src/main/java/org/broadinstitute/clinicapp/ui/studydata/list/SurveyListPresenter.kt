package org.broadinstitute.clinicapp.ui.studydata.list

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils
import java.util.*


class SurveyListPresenter(
    private var view: SDListContract.View,
    val context: Context,
    val pref: SharedPreferenceUtils
) :
    SDListContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    override fun checkStudyDataInDB(studyFormId: String, isServerUpdate: Boolean) {
        compositeDisposable.add(repository.getMasterStudyDatasCount(studyFormId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count ->

                if (count == 0) {
                    view.showEmptyWarning(true)
                    if (isServerUpdate) getStudyDataByFormIdFromAPI(
                        pref.readStringFromPref(
                            Constants.PrefKey.PREF_USER_NAME
                        )!!, studyFormId
                    )
                } else {
                    view.showEmptyWarning(false)
                    getStudyDataFromDB(studyFormId, "")
                }

            }
        )
    }

    override fun getStudyDataByFormIdFromAPI(userId: String, studyFormId: String) {
        if (Constants.API_ENABLED && NetworkUtils.isNetworkConnected(context)) {
            view.showProgressBar(true)
            compositeDisposable.add(repository.getStudyDataOnline(
                userId,
                studyFormId,
                Constants.LIMIT,
                0
            )
                .doOnSuccess { response ->
                    response.data?.forEach { it ->
                        repository.insertPatient(it.patient!!)
                        it.adminId = it.patient!!.adminId
                        it.isServerUpdated = true
                        repository.insertMasterStudyData(it)
                        it.studyDatas.forEach { studyData ->
                            studyData.tempMasterStudyDataId = it.tempMasterStudyDataId
                        }
                        repository.insertStudyData(it.studyDatas)
                    }

                }
                .subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe({
                    view.showProgressBar(false)
                    getStudyDataFromDB(studyFormId, "")
                },

                    { throwable ->
                        view.showProgressBar(false)
                        throwable.message?.let { view.showSnackBarMessage(CommonUtils.getErrorMessage(throwable)) }
                    }

                )
            )
        }
    }


    override fun searchStudyDataFromDB(search: String, tempId: String) {
        view.showProgressBar(true)
        compositeDisposable.add(getStudyDataObservable(search, tempId).subscribe({
            view.showProgressBar(false)
            if (it.isNotEmpty()) {
                view.showStudyData(it)
            }else{
                view.showToastMessage(context.getString(R.string.no_result_found))
            }
        },
            {
                view.showProgressBar(false)

            }
        )
        )
    }

    override fun getStudyDataFromDB(studyFormId: String, search: String) {
        view.showProgressBar(true)
        compositeDisposable.add(getStudyDataObservable(
            "",
            studyFormId
        ).subscribe({
            view.showProgressBar(false)
            if (it.isNotEmpty()) {
                view.showEmptyWarning(false)
//                val uniqueSurveyList: PagedList<MasterStudyData> = it.map { it.id }.distinct()
//                view.showStudyData(uniqueSurveyList)
                 view.showStudyData(it)
            }else {
                view.showEmptyWarning(true)
            }
        },
            {
                view.showProgressBar(false)

            }
        )
        )
    }

    private fun getStudyDataObservable(
        search: String,
        studyFormId: String): Flowable<PagedList<MasterStudyData>> {

        val pageSize = Constants.LIMIT

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(pageSize)
            .setPageSize(pageSize).build()

        val factory: DataSource.Factory<Int, MasterStudyData> = if (!TextUtils.isEmpty(search)) {
            repository.searchStudyDataFromDB(search,studyFormId)

        } else {
            repository.getStudyDataFromDB(studyFormId)

        }

        val pagedListBuilder: RxPagedListBuilder<Int, MasterStudyData> =
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

    override fun attach(view: SDListContract.View) {
        this.view = view
    }

    //===============================================NEW ADDITION FOR SurveyListSpecificActivity================

    override fun checkStudyDataSpecificInDB(studyFormId: String, masterStudyDataID: String, isServerUpdate: Boolean) {
        compositeDisposable.add(repository.getMasterStudyDatasSpecificCount(studyFormId, masterStudyDataID)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count ->

                if (count == 0) {
                    view.showEmptyWarning(true)
                    if (isServerUpdate) getStudyDataByFormIdFromAPI(
                        pref.readStringFromPref(
                            Constants.PrefKey.PREF_USER_NAME
                        )!!, studyFormId
                    )
                } else {
                    view.showEmptyWarning(false)
                    getStudyDataSpecificFromDB(studyFormId, masterStudyDataID, "")
                }

            }
        )
    }

    override fun getStudyDataSpecificFromDB(studyFormId: String, masterStudyDataID: String, search: String) {
        view.showProgressBar(true)
        compositeDisposable.add(getStudyDataSpecificObservable(
            "", masterStudyDataID,
            studyFormId
        ).subscribe({
            view.showProgressBar(false)
            if (it.isNotEmpty()) {
                view.showEmptyWarning(false)
//                val uniqueSurveyList: PagedList<MasterStudyData> = it.map { it.id }.distinct()
//                view.showStudyData(uniqueSurveyList)
                view.showStudyData(it)
            }else {
                view.showEmptyWarning(true)
            }
        },
            {
                view.showProgressBar(false)

            }
        )
        )
    }

    private fun getStudyDataSpecificObservable(
        search: String,
        masterStudyDataID: String,
        studyFormId: String): Flowable<PagedList<MasterStudyData>> {

        val pageSize = Constants.LIMIT

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(pageSize)
            .setPageSize(pageSize).build()

        val factory: DataSource.Factory<Int, MasterStudyData> = if (!TextUtils.isEmpty(search)) {
            repository.searchStudyDataFromDB(search,studyFormId)

        } else {
            repository.getStudySpecificDataFromDB(studyFormId, masterStudyDataID)

        }

        val pagedListBuilder: RxPagedListBuilder<Int, MasterStudyData> =
            RxPagedListBuilder(
                factory,
                pagedListConfig
            )

        pagedListBuilder.setFetchScheduler(Schedulers.io())
        pagedListBuilder.setNotifyScheduler(AndroidSchedulers.mainThread())
        return pagedListBuilder.buildFlowable(BackpressureStrategy.LATEST)

    }
    //===============================================NEW ADDITION FOR Specific Patient Data================

    override fun getPatients(search: String, studyFormId: String) {
        view.showProgress(true)
        compositeDisposable.add(getPatientObservable(search, studyFormId).subscribe({
                patients ->
            view.showProgress(false)
            if (patients.isNotEmpty()) {
                view.showPatients(patients)
            }else{
                if(search.isEmpty())
                    view.showEmptyWarning(true)
                else {
//                    view.showToastMessage(context.getString(R.string.no_result_found))
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

}