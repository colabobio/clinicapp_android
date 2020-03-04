package org.broadinstitute.clinicapp.ui.studyform

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.collection.arraySetOf
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail


class ItemPresenter(private var view: ItemContract.View, val context: Context) :
    ItemContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)
    private var offlineSearchItems = arrayListOf<String>()
    var mValues = arrayListOf<StudyFormDetail>()
    private var onlineTotalCount = 0
    private var dbTotalCount = 0
    var dbCount = 0
    private var onlineCurrentCount = 0
    var isLoading = true
    var offset = 0
    private var formRequestQueue = arraySetOf<String>()

   @SuppressLint("CheckResult")
    override
    fun getSearchedForms(query: String, offset: Int) {
        Log.v("getSearchedForms", "offset :$offset")
       if(offset == 0) {
           repository.searchStudyFormsDBCount(query).toObservable().
               subscribeOn(Schedulers.io())
               .doOnNext{ searchForms(query, offset)}
               .observeOn(Schedulers.io())
               .subscribe {
                   dbTotalCount = it
               }
       }else {
           searchForms(query, offset)
       }
   }

    private fun searchForms(query: String, offset: Int){
        compositeDisposable.add(
            repository.searchStudyFormsFromDB(query, Constants.LIMIT, offset)
                .subscribeOn(Schedulers.io())
                .doOnNext { it ->
                    it.forEach {
                        offlineSearchItems.add(it.masterStudyForms.tempMasterStudyFormsId.toString())
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ studyForms ->
                    isLoading = true
                    dbCount += studyForms.size
                    if (offset == 0) mValues.clear()
                    mValues.addAll(studyForms)

                    view.showStudyForm(mValues)

                },
                    { throwable ->
                       throwable.printStackTrace()
                    }
                ))
    }

    override fun getSearchedFormsOnline(query: String, offset: Int, showProgress: Boolean, userD : String?) {

        view.showProgress(showProgress)

        compositeDisposable.add(repository.searchStudyFormsOnline(query, Constants.LIMIT, offset, userD)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                // for import online functionality first time search of new data then clear data
                if (showProgress && offset == 0) mValues.clear()
                view.showProgress(false)
                isLoading = true

                val detailsList = ArrayList<StudyFormDetail>()
                onlineTotalCount = response.totalCount
                response.data?.forEach { forms ->

                    if (showProgress) {
                        val studyFormDetail =
                            StudyFormDetail(
                                forms,
                                forms.studyFormVariables
                            )
                        detailsList.add(studyFormDetail)
                    } else {
                        if (!offlineSearchItems.contains(forms.tempMasterStudyFormsId)) {
                            forms.isFromOffline = false
                            val studyFormDetail =
                                StudyFormDetail(
                                    forms,
                                    forms.studyFormVariables
                                )
                            detailsList.add(studyFormDetail)
                        }
                    }
                }
                // We are removing list element where logged user id is equal to creator of study form id
                onlineCurrentCount += response.data?.size!!
                mValues.addAll(detailsList)
                this.offset++
                view.showStudyForm(mValues)

            },
                { throwable ->
                    view.showProgress(false)
                   if(showProgress) view.handleThrowable(throwable)
                }
            ))
    }

    override fun loadMoreStudyForms(
        fromScreen: String, searchCriteria: String, isNetwork: Boolean, userID : String?) {

        if (fromScreen == Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM) {
            if(dbCount < dbTotalCount) getSearchedForms(searchCriteria, dbCount) else isLoading = false
            if (isNetwork && onlineCurrentCount < onlineTotalCount && searchCriteria.isNotEmpty())
                getSearchedFormsOnline(searchCriteria, offset, false, null)
            else isLoading = false
        } else if (isNetwork && onlineCurrentCount < onlineTotalCount) {
                getSearchedFormsOnline(
                    searchCriteria,
                    offset,true,userID)
            }
    }


    override fun unsubscribe() {
        compositeDisposable.clear()
        formRequestQueue.clear()
    }

    override fun attach(view: ItemContract.View) {
        this.view = view
    }
}