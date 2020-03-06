package org.broadinstitute.clinicapp.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.Constants.BundleKey.CALL_DETAILS_STUDY_FORM_KEY
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.OnSyncInteractionListener
import org.broadinstitute.clinicapp.ui.studydata.list.SurveyListActivity
import org.broadinstitute.clinicapp.ui.studyform.details.DetailsActivity
import org.broadinstitute.clinicapp.util.CommonUtils

class StudyFormsAdapter(val userId: String, val syncInteractionListener: OnSyncInteractionListener
): PagedListAdapter<StudyFormDetail, StudyFormsAdapter.StudyFormViewHolder>(DiffCallback())
   {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyFormViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StudyFormViewHolder(layoutInflater, parent)
    }

     override fun onBindViewHolder(holder: StudyFormViewHolder, position: Int) {
        val masterStudyForm: StudyFormDetail? = getItem(position)
        masterStudyForm?.let { holder.bind(it) }
    }


    inner class StudyFormViewHolder(inflater: LayoutInflater, val parent: ViewGroup) :

        RecyclerView.ViewHolder(inflater.inflate(R.layout.row_study_form, parent, false)) {

        private var txtStudyFormTitle: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyFormTitle)
        private var txtStudyFormDescription: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyFormDescription)
        private var txtStudyFormCreator: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyFormCreator)
        private var txtStudyFormCreatedOn: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyFormCreatedOn)
        private var imgEdit: AppCompatImageView = itemView.findViewById(R.id.imgEditStudyForm)
        private var imgSync: AppCompatImageView = itemView.findViewById(R.id.imgSyncStatus)

        fun bind(detail: StudyFormDetail) {

            val masterStudyForm = detail.masterStudyForms
            txtStudyFormTitle.text = masterStudyForm.title
            txtStudyFormDescription.text = masterStudyForm.description
            txtStudyFormCreator.text = String.format(
                parent.context.getString(R.string.created_by),
                masterStudyForm.creator
            )
            txtStudyFormCreatedOn.text = CommonUtils.convertDate(masterStudyForm.lastModified)

            if (masterStudyForm.userId != userId) {
                imgEdit.visibility = View.VISIBLE
                imgEdit.setImageResource(R.drawable.ic_visibility_24dp)
            } else {
                imgEdit.setImageResource(R.drawable.ic_edit)
                imgEdit.visibility = View.VISIBLE
            }

            if (masterStudyForm.isServerUpdated) {
                imgSync.setImageResource(R.drawable.ic_cloud_check_done)
            } else {
                imgSync.setImageResource(R.drawable.ic_cloud_reload_sync)
            }

            imgEdit.setOnClickListener {
                val intent = Intent(parent.context, DetailsActivity::class.java)
                intent.putExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, detail)
                intent.putExtra(
                    CALL_DETAILS_STUDY_FORM_KEY,
                    Constants.CallingPageValue.HOME_EDIT_STUDY_FORM
                )
                parent.context.startActivity(intent)
            }

            itemView.setOnClickListener {
                val intent = Intent(parent.context, SurveyListActivity::class.java)
                intent.putExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, detail)
                intent.putExtra(
                    CALL_DETAILS_STUDY_FORM_KEY,
                    Constants.CallingPageValue.HOME_EDIT_STUDY_FORM
                )
                parent.context.startActivity(intent)
            }

            imgSync.setOnClickListener {
                if(!masterStudyForm.isServerUpdated)syncInteractionListener.onSyncClick(detail)
            }
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<StudyFormDetail>() {

        override fun areContentsTheSame(oldItem: StudyFormDetail, newItem: StudyFormDetail): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: StudyFormDetail, newItem: StudyFormDetail): Boolean {
            return oldItem.masterStudyForms.id == newItem.masterStudyForms.id
        }
    }
}