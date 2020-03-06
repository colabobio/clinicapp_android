package org.broadinstitute.clinicapp.ui.studyform

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_item_list.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.OnSyncInteractionListener
import org.broadinstitute.clinicapp.util.CommonUtils


class ItemFragment : BaseFragment(), ItemContract.View, OnSyncInteractionListener
{
    private var fromScreen: String = Constants.BundleKey.EXISTING_STUDY_FORM_KEY
    private var listener: OnListFragmentInteractionListener? = null
    private lateinit var searchView: SearchView
    lateinit var presenter: ItemPresenter
    lateinit var adapter: SearchRecyclerViewAdapter
    private lateinit var emptyView: TextView
    private var isSearchResultLoaded = false
    private var searchCriteria: String = ""

    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        presenter = ItemPresenter(this, this.requireContext())

        arguments?.let {
            fromScreen = it.getString(ARG_SEARCH_TYPE).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)

        val recyclerView = view.item_recyclerView

        // Set the adapter
        adapter = SearchRecyclerViewAdapter(presenter.mValues, listener, this, fromScreen)
        view.item_recyclerView.adapter = adapter
        emptyView = view.form_emptyView
        if (recyclerView is RecyclerView) {

            if (fromScreen == CREATE_FROM_TEMPLATE_STUDY_FORM) {
                view.fragment_item_header.text = getString(R.string.create_new_form_template)
                presenter.getSearchedForms(searchCriteria, 0)
            } else {
                view.fragment_item_header.text = getString(R.string.import_study_form)
                presenter.getSearchedFormsOnline("", 0, true, userID)
            }

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                        if (presenter.isLoading) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                presenter.isLoading = false
                                presenter.loadMoreStudyForms(fromScreen, searchCriteria, isNetworkConnected, userID)

                            }
                        }
                    }
                }
            })



        }
        return view
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    interface OnListFragmentInteractionListener {

        fun onListFragmentInteraction(item: StudyFormDetail)
    }

    override fun onSyncClick(item: StudyFormDetail) {
        if(isNetworkConnected)presenter.syncIndividualForm(item)
        else showSnackBarMessage(getString(R.string.network_error))
    }


    companion object {

        const val ARG_SEARCH_TYPE = "search-type"

        const val REQUEST_DETAILS_CODE = 100

        @JvmStatic
        fun newInstance(fromScreen: String) =
            ItemFragment().apply {
                arguments = Bundle().apply {
                   putString(ARG_SEARCH_TYPE, fromScreen)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_search_form, menu)
        val searchItem = menu.findItem(R.id.action_search).apply {
            //  isVisible = fromScreen == Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM
        }
        searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        val searchSubmit =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchSubmit.setImageResource(R.mipmap.ic_search)
        searchView.queryHint = getString(R.string.search_study_forms)
        searchView.setOnSearchClickListener {
            if (isNetworkConnected) searchView.queryHint = getString(R.string.search_online_study_forms)
            else searchView.queryHint = getString(R.string.search_study_forms)
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    presenter.dbCount = 0
                    if (fromScreen == CREATE_FROM_TEMPLATE_STUDY_FORM) {
                        presenter.getSearchedForms("", presenter.dbCount)
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {

                presenter.offset = 0
                presenter.dbCount = 0
                searchCriteria = query.trim()
                isSearchResultLoaded = true
                if (fromScreen == CREATE_FROM_TEMPLATE_STUDY_FORM) {
                    presenter.getSearchedForms("%$query%", presenter.dbCount)
                    if(isNetworkConnected)presenter.getSearchedFormsOnline("%$query%", presenter.offset, false, null)
                } else {
                    if(isNetworkConnected)presenter.getSearchedFormsOnline("%$query%", presenter.offset, true, userID)
                        else onError(R.string.network_error)
                }

                return true
            }
        })

        searchView.setOnCloseListener { !isSearchResultLoaded }

    }

    override fun showStudyForm(list: ArrayList<StudyFormDetail>) {
        if(presenter.mValues.isEmpty()){
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }



    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else hideLoading()
    }

    override fun showSnackBarMessage(message: String) {
        onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }



    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    override fun handleThrowable(throwable: Throwable) {

        val errorMsg = CommonUtils.getErrorMessage(throwable)
        if(errorMsg == getString(R.string.unAuthorized_error)) {
            handleAuthError()
        }else{
            showSnackBarMessage(errorMsg)
        }
    }

}
