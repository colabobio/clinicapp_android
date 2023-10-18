package org.broadinstitute.clinicapp.ui.studydata.list

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.MasterStudyData
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.survey.SurveyActivity
import org.broadinstitute.clinicapp.util.CommonUtils

class StudyDataSpecificAdapter(
    private val formDetail: StudyFormDetail
) : PagedListAdapter<MasterStudyData, StudyDataSpecificAdapter.StudyDataViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyDataViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StudyDataViewHolder(layoutInflater, parent, formDetail)
    }

    override fun onBindViewHolder(holder: StudyDataViewHolder, position: Int) {
        val masterStudyData: MasterStudyData? = getItem(position)
        masterStudyData?.let { holder.bind(it) }
    }

    inner class StudyDataViewHolder(
        inflater: LayoutInflater,
        val parent: ViewGroup,
        private val formDetail: StudyFormDetail
    ) :

        RecyclerView.ViewHolder(inflater.inflate(R.layout.row_study_data, parent, false)) {
        val userId = ClinicApp.getUserName()

        private var txtPatientId: AppCompatTextView = itemView.findViewById(R.id.txtPatientId)
        private var txtCreatedOn: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyDataCreatedOn)
        private var txtLastModified: AppCompatTextView =
            itemView.findViewById(R.id.txtStudyDataLastModified)
        private var txtPatientType: AppCompatTextView =
            itemView.findViewById(R.id.txtPatientType)
        private var imgSync: AppCompatImageView = itemView.findViewById(R.id.imgSyncStatus)

        fun bind(masterStudyData: MasterStudyData) {

            txtPatientId.text = masterStudyData.adminId
            txtCreatedOn.text = parent.context.getString(
                R.string.created_on,
                CommonUtils.convertDate(masterStudyData.createdOn)
            )
            txtLastModified.text = parent.context.getString(
                R.string.last_modified,
                CommonUtils.convertDate(masterStudyData.lastModified)
            )

            if (Constants.API_ENABLED) {
                imgSync.visibility = View.VISIBLE
                if (masterStudyData.isServerUpdated) {
                    imgSync.setImageResource(R.drawable.ic_cloud_check_done)
                } else {
                    imgSync.setImageResource(R.drawable.ic_cloud_reload_sync)
                }
            } else {
                imgSync.visibility = View.INVISIBLE
            }

            when {
                masterStudyData.studyDataWhenAsked == 0 -> {
                    txtPatientType.text = parent.context.getString(R.string.when_asked_0)
                    txtPatientType.setTextColor(parent.context.getColor(R.color.colorAccent))
                }
                masterStudyData.studyDataWhenAsked == 1 -> {
                    txtPatientType.text = parent.context.getString(R.string.study_data_followup)
                    txtPatientType.setTextColor(parent.context.getColor(R.color.colorPrimaryDark))
                }
                masterStudyData.studyDataWhenAsked == 2 -> {
                    txtPatientType.text = parent.context.getString(R.string.study_data_final_outcome)
                    txtPatientType.setTextColor(parent.context.getColor(R.color.black))
                }
            }

            itemView.setOnClickListener {
                val intent = Intent(parent.context, SurveyActivity::class.java)
                    .putExtra(
                        Constants.BundleKey.CREATE_STUDY_DATA_KEY,
                        Constants.StudyDataType.UPDATE_STUDY_DATA
                    )
                    .putExtra(
                        Constants.BundleKey.STUDY_FORM_DETAIL_KEY,
                        formDetail
                    )
                    .putExtra(
                        Constants.BundleKey.MASTER_STUDY_DATA_KEY,
                        masterStudyData

                    )
                parent.context.startActivity(intent)
            }

        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MasterStudyData>() {

        override fun areContentsTheSame(
            oldItem: MasterStudyData,
            newItem: MasterStudyData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: MasterStudyData, newItem: MasterStudyData): Boolean {
            return oldItem.id == newItem.id
        }
    }
}