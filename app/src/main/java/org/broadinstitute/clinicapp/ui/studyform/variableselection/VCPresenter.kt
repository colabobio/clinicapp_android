package org.broadinstitute.clinicapp.ui.studyform.variableselection

import android.content.Context
import android.util.Log
import com.google.gson.Gson
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
import org.json.JSONArray
import java.io.IOException


class VCPresenter(private var view: VCContract.View, val context: Context) : VCContract.Presenter {
    private var disposable: Disposable? = null
    val source = PublishSubject.create<ArrayList<List<MasterVariables>>>()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val repository =
        ClinicRepository.getInstance(context)

    override fun getCategories(limit: Int, offset: Int) {
        val variablesList = HashMap<String, List<MasterVariables>>()
        val catList = ArrayList<String>()

        var gson = Gson()
        val jsonFileString = context?.let { getJsonDataFromAsset(it, "variableListDataMain1.json") }
        val jsonArr = JSONArray(jsonFileString)

        val listOfAllMasterVariables = mutableListOf<MasterVariables>()
        for (docs in 0 until jsonArr.length()) {
            val jsonObj = jsonArr.getJSONObject(docs)
            var masterVariables = gson.fromJson(jsonObj.toString(), MasterVariables::class.java)
            listOfAllMasterVariables.add(masterVariables)
            Log.d("JSON VALUE", masterVariables.toString())
        }

        for (variables in 0 until listOfAllMasterVariables.size) {
            if(!catList.contains(listOfAllMasterVariables.get(variables).variableCategory))
                catList.add(listOfAllMasterVariables.get(variables).variableCategory)
        }
        Log.d("catList", catList.toString())
        for (catVariables in 0 until catList.size) {
            var listOfVariables: List<MasterVariables> = emptyList()
            var resultObjects = arrayListOf<MasterVariables>()
            for (variables in 0 until listOfAllMasterVariables.size) {
                if (listOfAllMasterVariables.get(variables).variableCategory == catList.get(catVariables)) {
                    resultObjects.add(listOfAllMasterVariables.get(variables))
//                    results: List<MasterVariables>  = resultObjects.Cast<string>().ToList();
                    listOfVariables = resultObjects.toList()
                }
            }
            variablesList.put(catList.get(catVariables), listOfVariables)
//            Log.d("resultObjects", resultObjects.toString())
//            Log.d("listOfVariables", listOfVariables.toString())
//            Log.d("variablesList", variablesList.toString())
//            Log.d("catList", catList.toString())
        }
//        Log.d("resultObjects", resultObjects.toString())
//        Log.d("listOfVariables", listOfVariables.toString())
//        Log.d("variablesList1", variablesList.toString())
//        Log.d("catList1", catList.toString())

        view.showMasterVariables(variablesList, catList)


//        compositeDisposable.add(repository.getCategories()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ categories ->
//                getCategoriesObservable(categories)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(Schedulers.computation())
//                    .flatMap { category -> getMVListObservable(category) }
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(object : Observer<List<MasterVariables>> {
//                        override fun onNext(variables: List<MasterVariables>) {
//                            variablesList[variables[0].variableCategory] = variables
//                            catList.add(variables[0].variableCategory)
//                        }
//
//                        override fun onComplete() {
//
//                            view.showMasterVariables(variablesList, catList)
//                        }
//
//                        override fun onError(e: Throwable) {
//
//                        }
//
//                        override fun onSubscribe(d: Disposable) {
//                            disposable = d
//                        }
//
//                    })
//            },
//                { throwable ->
//
//                    throwable.printStackTrace()
//                }
//
//            ))
    }
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
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