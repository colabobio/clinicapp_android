package org.broadinstitute.clinicapp.ui.studydata.list

import android.content.Context
import android.text.TextUtils
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
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils


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
                    if(isServerUpdate)getStudyDataByFormIdFromAPI(
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
        if (NetworkUtils.isNetworkConnected(context)) {
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
}