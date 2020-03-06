package org.broadinstitute.clinicapp.ui.studyform


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_study_form.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.OnSyncInteractionListener
import org.broadinstitute.clinicapp.ui.studyform.ItemFragment.OnListFragmentInteractionListener
import org.broadinstitute.clinicapp.util.CommonUtils


class SearchRecyclerViewAdapter(
    private var mValues: ArrayList<StudyFormDetail>,
    private val mListener: OnListFragmentInteractionListener?, private val syncListener: OnSyncInteractionListener,
    private val fromScreen : String
) : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {
    private lateinit var context: Context
    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as StudyFormDetail
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_study_form, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mTitleView.text = item.masterStudyForms.title
        holder.mDescView.text = item.masterStudyForms.description
        holder.mCreateView.text =
            String.format(context.getString(R.string.created_by, item.masterStudyForms.creator))
        holder.mDateView.text = item.masterStudyForms.lastModified.let { CommonUtils.convertDate(it) }


        if(fromScreen == Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM) {
            holder.mSyncView.visibility = View.VISIBLE
            if(item.masterStudyForms.isFromOffline) {
                if (item.masterStudyForms.isServerUpdated)
                    holder.mSyncView.setImageResource(R.drawable.ic_cloud_check_done)
                else holder.mSyncView.setImageResource(R.drawable.ic_cloud_reload_sync)
            }else holder.mSyncView.setImageResource(R.drawable.ic_cloud_check_done)

            holder.mSyncView.setOnClickListener {
                if(!item.masterStudyForms.isServerUpdated){
                    syncListener.onSyncClick(item)
                }
            }
        } else{
            holder.mSyncView.setImageResource(R.drawable.ic_cloud_check_done)
        }

        holder.mEditView.visibility = View.INVISIBLE
        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size



    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView = mView.txtStudyFormTitle
        val mDescView: TextView = mView.txtStudyFormDescription
        val mCreateView: TextView = mView.txtStudyFormCreator
        val mDateView: TextView = mView.txtStudyFormCreatedOn
        val mSyncView: ImageView = mView.imgSyncStatus
        val mEditView: ImageView = mView.imgEditStudyForm

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }

}
