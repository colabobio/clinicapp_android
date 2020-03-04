package org.broadinstitute.clinicapp.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.collection.arraySetOf
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.dao.SyncStatusDao
import org.broadinstitute.clinicapp.data.source.local.entities.*
import org.broadinstitute.clinicapp.data.source.remote.*
import org.broadinstitute.clinicapp.util.CommonUtils
import org.broadinstitute.clinicapp.util.NetworkUtils
import org.broadinstitute.clinicapp.util.SharedPreferenceUtils
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.data.source.ClinicRepository
import kotlin.math.ceil


class HomePresenter(
    private var view: HomeContract.View,
    val context: Context,
    private val preferenceUtils: SharedPreferenceUtils
) :
    HomeContract.Presenter {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository = ClinicRepository.getInstance(context)

    var allLastSyncDate: SyncStatusDao.AllLastSyncDate = SyncStatusDao.AllLastSyncDate(0, 0, 0)

    private val userName =
        preferenceUtils.readStringFromPref(Constants.PrefKey.PREF_USER_NAME).toString()

    var source = PublishSubject.create<SyncStats>()
    data class SyncStats(val show : Boolean, val msg : String)

    private var formRequestQueue = arraySetOf<String>()

    @SuppressLint("CheckResult")
    fun getSyncTimes() {

        compositeDisposable.add(repository.getAllLastSyncDate()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .toObservable().subscribe {
                allLastSyncDate = it
                getMasterVariables(0, allLastSyncDate.lastModifiedVariable, false)
            })


    }


    override fun getMyStudyFormsFromAPI(isSingleCall: Boolean) {

        if (NetworkUtils.isNetworkConnected(context)) {

            compositeDisposable.add(repository.getMyStudyFormsOnline(
                userName,
                allLastSyncDate.lastModifiedForms,
                Constants.LIMIT,
                0
            )
                .doOnSuccess { response ->
                    insertMasterForms(response)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    getStudyFormsFromDB("")

                    if (!isSingleCall) {
                        val totalCount = it.totalCount
                        if(totalCount == 0) {
                            source.onNext(SyncStats(true, "Empty Forms is returned"))
                            getAllStudyDataAPI(
                                userName,
                                allLastSyncDate.lastModifiedStudies
                            )
                        } else {
                            getParentFormsObservable(totalCount)
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .flatMap { f ->
                                    getChildFormsObservable(
                                        f,
                                        userName,
                                        allLastSyncDate.lastModifiedForms
                                    )
                                }
                                .observeOn(Schedulers.io())
                                .subscribe(object : Observer<StudyFormsResponse> {
                                    override fun onNext(variables: StudyFormsResponse) {

                                        insertMasterForms(variables)
                                    }

                                    override fun onComplete() {

                                        source.onNext(SyncStats(true, "Get Forms Sync Completed"))
                                        getAllStudyDataAPI(
                                            userName,
                                            allLastSyncDate.lastModifiedStudies
                                        )

                                    }

                                    override fun onError(e: Throwable) {
                                       // Handle error of Auth
                                        source.onNext(SyncStats(false, "Forms Sync failed"))
                                    }

                                    override fun onSubscribe(d: Disposable) {
                                    }
                                })
                        }

                    } else {
                         view.showProgress(false)
                    }
                },

                    { throwable ->

                        if(isSingleCall){
                            view.showProgress(false)
                            // Handle error of Auth
                            view.showSnackBarMessage(throwable.localizedMessage!!)
                        }
                        else  source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))


                    }

                )
            )
        }
    }

    private fun insertMasterForms(response: StudyFormsResponse) {

        try {
            response.data?.forEach {
                try {
                    it.isServerUpdated = true
                    val id = repository.insertMasterStudyForm(it)
                    it.id = id
                    it.studyFormVariables.forEach { variable ->
                        variable.isServerUpdated = true
                        variable.masterStudyFormsIdFk = it.tempMasterStudyFormsId.toString()
                    }
                    repository.insertStudyFormVariables(it.studyFormVariables)
                  //  Log.e("Forms variable", "size $variableId")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if(response.data?.size!! > 0){
                val lastModified = repository.getLastModifiedForMSF()
                 repository.insertSyncStatus(
                    SyncStatus(
                        tableName = "MasterStudyForms",
                        lastSyncTime = lastModified
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getParentFormsObservable(totalCount: Int): Observable<Int> {

        return Observable
            .create(ObservableOnSubscribe<Int> { emitter ->

                val loopCount = ceil(totalCount.toDouble() / Constants.LIMIT)
                for (index in 1 until loopCount.toInt()) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(index)
                    }

                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    private fun getChildFormsObservable(
        pageNo: Int,
        userId: String,
        lastModified: Long
    ): Observable<StudyFormsResponse> {

        return repository.getMyStudyFormsOnline(
            userId, lastModified, Constants.LIMIT, pageNo
        ).toObservable()
    }

    override fun getMasterVariables(pageNo: Int, lastModified: Long, isInitialLoad: Boolean) {

        if (NetworkUtils.isNetworkConnected(context)) {
                if(isInitialLoad)view.showProgress(true)
                compositeDisposable.add(repository.getMasterVariablesFromAPI(
                allLastSyncDate.lastModifiedVariable,
                Constants.MASTER_VARIABLES_LIMIT,
                pageNo
            )
                .doOnSuccess { response ->
                    if(response.data.isNotEmpty())insertMasterVariable(response)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    val totalCount = it.totalCount

                    if(totalCount == 0){
                        if (!isInitialLoad) {
                            source.onNext(SyncStats(true,"Master variable total count is empty"))
                            getUnSyncedForms()
                        }
                        else {
                            checkStudyFormsInDB(isInitialLoad)
                        }
                    }
                    else {
                        // Reduce call of check master variable on Home page
                        preferenceUtils.writeBooleanToPref(Constants.PrefKey.PREF_INITIAL_LAUNCH, false)
                        getParentVariableObservable(totalCount)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .flatMap { f ->
                                getChildVariableObservable(
                                    f,
                                    allLastSyncDate.lastModifiedVariable
                                )
                            }
                            .observeOn(Schedulers.io())
                            .subscribe(object : Observer<MasterVariablesResponse> {
                                override fun onNext(variables: MasterVariablesResponse) {
                                    insertMasterVariable(response = variables)
                                }

                                override fun onComplete() {
                                    if (!isInitialLoad) {
                                        source.onNext(SyncStats(true,"Master variable sync complete"))
                                         getUnSyncedForms()

                                    }
                                    else {
                                        checkStudyFormsInDB(isInitialLoad)
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    if(isInitialLoad) view.showProgress(false)
                                    else source.onNext(SyncStats(false, CommonUtils.getErrorMessage(e)))

                                }

                                override fun onSubscribe(d: Disposable) {
                                }

                            })
                    }
                },

                    { throwable ->
                        if(isInitialLoad) view.showProgress(false)
                        else  source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))

                    }

                )

            )

        }

    }

    private fun insertMasterVariable(response: MasterVariablesResponse) {
        try {
            if(response.data.isNotEmpty()){
                repository.insertMasterVariables(response.data)
                val lastModified = repository.getLastModifiedForMV()
                repository.insertSyncStatus(
                    SyncStatus(
                        tableName = "MasterVariables",
                        lastSyncTime = lastModified
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getParentVariableObservable(totalCount: Int): Observable<Int> {

        return Observable
            .create(ObservableOnSubscribe<Int> { emitter ->

                val loopCount = ceil(totalCount.toDouble() / Constants.MASTER_VARIABLES_LIMIT)
                for (index in 1 until loopCount.toInt()) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(index)
                    }

                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    private fun getChildVariableObservable(
        pageNo: Int,
        lastModified: Long
    ): Observable<MasterVariablesResponse> {

        return repository.getMasterVariablesFromAPI(
            lastModified,
            Constants.MASTER_VARIABLES_LIMIT,
            pageNo
        ).toObservable()
    }


    private fun getAllStudyDataAPI(userId: String, lastModified: Long) {
        if (NetworkUtils.isNetworkConnected(context)) {

            compositeDisposable.add(repository.getStudyAllDataOnline(
                userId,
                lastModified,
                Constants.STUDY_DATA_LIMIT,
                0
            )
                .doOnSuccess(this::insertMasterStudyData)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({

                    val totalCount = it.totalCount
                    if (totalCount > 0) {
                        getParentStudyDataObservable(totalCount)
                            .subscribeOn(Schedulers.io())
                            .observeOn(Schedulers.io())
                            .flatMap { f ->
                                getChildStudyDataObservable(
                                    f,
                                    userId,
                                    allLastSyncDate.lastModifiedStudies
                                )
                            }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : Observer<StudyDataResponse> {
                                override fun onNext(variables: StudyDataResponse) {
                                   insertMasterStudyData(variables)
                                }

                                override fun onComplete() {
                                    source.onNext(SyncStats(true,"Get All study data Completed"))
                                }

                                override fun onError(e: Throwable) {
                                    // Handle error of Auth
                                    source.onNext(SyncStats(false, CommonUtils.getErrorMessage(e)))

                                }

                                override fun onSubscribe(d: Disposable) {
                                }
                            })
                    } else {
                        source.onNext(SyncStats(true,"Get All study data Completed"))
                    }
                },

                    { throwable ->
                        view.showProgress(false)
                        source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))

                    }

                )
            )
        }
    }

    private fun insertMasterStudyData(response: StudyDataResponse) {

        try {
            response.data?.forEach {
                try {
                    repository.insertPatient(it.patient!!)
                    it.adminId = it.patient!!.adminId
                    it.isServerUpdated = true
                    it.studyDatas.forEach { studyData ->
                        studyData.tempMasterStudyDataId = it.tempMasterStudyDataId

                    }
                    repository.insertMasterStudyData(it)
                    repository.insertStudyData(it.studyDatas)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (response.data?.size!! > 0) {
                val lastModified = repository.getLastModifiedForMSD()
                repository.insertSyncStatus(
                    SyncStatus(
                        tableName = "MasterStudyData",
                        lastSyncTime = lastModified
                    )
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getParentStudyDataObservable(totalCount: Int): Observable<Int> {

        return Observable
            .create(ObservableOnSubscribe<Int> { emitter ->

                val loopCount = ceil(totalCount.toDouble() / Constants.LIMIT)
                for (index in 1 until loopCount.toInt()) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(index)
                    }

                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    private fun getChildStudyDataObservable(
        pageNo: Int,
        userId: String,
        lastModified: Long
    ): Observable<StudyDataResponse> {
        return repository.getStudyAllDataOnline(
            userId, lastModified, Constants.STUDY_DATA_LIMIT, pageNo
        ).toObservable()
    }

    // Create Sync call

    override fun getUnSyncedForms() {
        compositeDisposable.add(repository.getNewStudyFormsFromDB()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

            .subscribe({ forms ->
                if (forms.isNotEmpty()) {
                    getLimitedFormsObservable(forms)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .flatMap { paginatedForms -> getFormAPIResponseObservable(paginatedForms) }
                        .observeOn(Schedulers.io())
                        .subscribe(object : Observer<CreateStudyFormsResponse> {
                            override fun onNext(response: CreateStudyFormsResponse) {
                              //  Log.e("StudyForm Resp success ", "" + response.success?.size)
                                response.success?.forEach { tempId ->
                                    try {
                                        repository.updateMasterStudyForm(tempId)
                                        repository.updateStudyFormVariables(tempId)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                Log.e("StudyForm Resp success ", "" + response.failed?.size)

                            }

                            override fun onComplete() {
                                Log.e("HomePresenter", "Study forms onComplete()")
                                source.onNext(SyncStats(true,"Sync form Completed"))
                                getUnSyncedStudyData()
                            }

                            override fun onError(e: Throwable) {
                                source.onNext(SyncStats(false, CommonUtils.getErrorMessage(e)))

                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                        })
                } else {
                    source.onNext(SyncStats(true,"Sync form not found in DB"))
                    getUnSyncedStudyData()
                }
            },

                { throwable ->
                     source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))

                }

            )
        )
    }

    private fun getLimitedFormsObservable(forms: List<MasterStudyForms>): Observable<List<MasterStudyForms>> {

        return Observable
            .create(ObservableOnSubscribe<List<MasterStudyForms>> { emitter ->

                val loopCount = ceil(forms.size.toDouble() / Constants.LIMIT.toDouble()).toInt()
                var lowerLimit = 0
                var upperLimit = Constants.LIMIT

                for (index in 0 until loopCount) {
                    val formsSublist = forms.subList(
                        lowerLimit,
                        if (upperLimit > forms.size) forms.size else upperLimit
                    )
                    for (form in formsSublist) {
                        val variables =
                            repository.getUpdatedFormVariables(form.tempMasterStudyFormsId!!)
                        form.studyFormVariables = variables
                    }
                    if (!emitter.isDisposed) {
                        emitter.onNext(formsSublist)
                    }
                    lowerLimit = upperLimit
                    upperLimit = lowerLimit + Constants.LIMIT
                }
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }).subscribeOn(Schedulers.io())
    }

    private fun getFormAPIResponseObservable(forms: List<MasterStudyForms>): Observable<CreateStudyFormsResponse> {
        return repository.createStudyFormsFromAPI(forms).toObservable()
    }

    private fun getUnSyncedStudyData() {
        compositeDisposable.add(repository.getNewPatientsFromDB()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())

            .subscribe({ patients ->
                if (patients.isNotEmpty()) {
                    getMasterStudyDataObservable(patients)
                        .observeOn(Schedulers.io())
                        .subscribe(object : Observer<List<StudyDataWithPatient>> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(studies: List<StudyDataWithPatient>) {
                                createStudyDataOnline(studies)
                            }

                            override fun onError(e: Throwable) {
                                source.onNext(SyncStats(false, CommonUtils.getErrorMessage(e)))

                            }

                            override fun onComplete() {
                                source.onNext(SyncStats(true, "Study data UnSync Completed"))
                                getMyStudyFormsFromAPI(false)
                            }
                        })
                } else {
                    source.onNext(SyncStats(true, "data UnSync not found in DB"))
                    getMyStudyFormsFromAPI(false)
                }
            },

                { throwable ->
                    source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))
                }

            )
        )
    }

    private fun getMasterStudyDataObservable(
        patients: List<Patient>
    ): Observable<List<StudyDataWithPatient>> {

        return Observable
            .create(ObservableOnSubscribe<List<StudyDataWithPatient>> { emitter ->

                val loopCount = ceil(patients.size.toDouble() / Constants.STUDY_DATA_LIMIT.toDouble()).toInt()
                var lowerLimit = 0
                var upperLimit = Constants.STUDY_DATA_LIMIT

                for (index in 0 until loopCount) {
                    val finalList = ArrayList<StudyDataWithPatient>()
                    val patientsSublist = patients.subList(
                        lowerLimit,
                        if (upperLimit > patients.size) patients.size else upperLimit
                    )
                    patientsSublist.forEach { patient ->
                        val masterDataList = repository.getNewStudyDataFromDB(patient.adminId)
                        masterDataList.forEach { masterStudyData ->
                            val dataList =
                                repository.getStudyData(masterStudyData.tempMasterStudyDataId)
                                masterStudyData.studyDatas = dataList
                        }
                        val studyDataWithPatient =
                            StudyDataWithPatient(
                                patient,
                                masterDataList
                            )
                        finalList.add(studyDataWithPatient)
                    }
                    lowerLimit = upperLimit
                    upperLimit = lowerLimit + Constants.STUDY_DATA_LIMIT
                    if (!emitter.isDisposed) {
                        emitter.onNext(finalList)
                    }
                }

                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }

            }).subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
    }

    private fun createStudyDataOnline(list: List<StudyDataWithPatient>) {

        val studyDataRequest = StudyDataRequest()
        studyDataRequest.studyDataRequests = list
        compositeDisposable.add(repository.createStudyDataFromAPI(studyDataRequest)
            .doOnSuccess { response ->
                response.success?.forEach { tempId ->
                    repository.updateMasterStudyData(tempId)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ response ->
                Log.e("StudyData Resp success ", "" + response.success?.size)
                Log.e("StudyData Resp failed ", "" + response.failed?.size)
            },

                { throwable ->
                    throwable.printStackTrace()
                    // Handle error of Auth
                   // source.onNext(SyncStats(false, CommonUtils.getErrorMessage(throwable)))
                }

            )

        )
    }


    override fun checkMasterVariables(offset: Int) {
        compositeDisposable.add(repository.getMasterVariablesCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ count ->
                if (count == 0) {
                    getMasterVariables(offset, 0, true)
                } else {
                    checkStudyFormsInDB(true)
                }
            },
                {
                    view.showProgress(false)

                }

            )
        )
    }

    override fun checkStudyFormsInDB(isSingleCall: Boolean) {
        compositeDisposable.add(repository.getMasterStudyFormsCount()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ count ->
                if (count == 0) {
                    view.showEmptyWarning(true)
                    view.showProgress(true)
                    getMyStudyFormsFromAPI(isSingleCall)
                } else {
                    view.showEmptyWarning(false)
                    view.showProgress(false)
                    getStudyFormsFromDB("")
                }
            },

                {
                }

            )
        )
    }

    override fun checkUnSyncData() {
        view.showProgress(true)
        compositeDisposable.add(repository.getAllUnSyncCounts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

            .subscribe({ count ->
                if (count.formCount > 0 || count.dataCount > 0) {
                    view.handleLogout(context.getString(R.string.logout_warning_msg), context.getString(R.string.logout_option),
                        context.getString(R.string.cancel))
                } else {
                    view.handleLogout(context.getString(R.string.normal_logout_warning_msg), context.getString(R.string.yes),
                        context.getString(R.string.no))
                }
            },
                { e ->
                    e.printStackTrace()
                    view.showProgress(false)

                }

            )
        )
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
        formRequestQueue.clear()
    }

    override fun attach(view: HomeContract.View) {
        this.view = view
    }


    override fun getStudyFormsFromDB(search: String) {

        compositeDisposable.add(getFormsObservable(search).subscribe({ studyForms ->
            if (studyForms.isNotEmpty()) {
                view.showStudyForms(studyForms)
            }else{
                // handle for search criteria
                if(search.isNotEmpty()){
                    view.showToastMessage(context.getString(R.string.no_result_found))
                }
            }
        },
            {

            }
        )
        )

    }

    private fun getFormsObservable(search: String): Flowable<PagedList<StudyFormDetail>> {
        val pageSize = Constants.LIMIT

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(pageSize)
            .setPageSize(pageSize).build()

        val factory: DataSource.Factory<Int, StudyFormDetail> =
            repository.searchStudyFormsFromDBItem(search)

        val pagedListBuilder: RxPagedListBuilder<Int, StudyFormDetail> =
            RxPagedListBuilder(
                factory,
                pagedListConfig
            )

        pagedListBuilder.setFetchScheduler(Schedulers.io())
        pagedListBuilder.setNotifyScheduler(AndroidSchedulers.mainThread())
        return pagedListBuilder.buildFlowable(BackpressureStrategy.LATEST)

    }

}