package org.broadinstitute.clinicapp.ui.studydata.patient

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.Patient
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.survey.SurveyActivity

class PatientAdapter(
    private var activity: AppCompatActivity,
    private val formDetail: StudyFormDetail,
    private val type: Int
) :
    PagedListAdapter<Patient, PatientAdapter.PatientViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PatientViewHolder(layoutInflater, parent, formDetail, type, activity)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient: Patient? = getItem(position)
        patient?.let { holder.bind(it) }
    }

    class PatientViewHolder(
        inflater: LayoutInflater,
        val parent: ViewGroup,
        private val formDetail: StudyFormDetail,
        private val type: Int,
        val activity: AppCompatActivity
    ) :

        RecyclerView.ViewHolder(inflater.inflate(R.layout.row_patient, parent, false)) {
        val userId = ClinicApp.getUserName()

        private var txtPatientId: AppCompatTextView = itemView.findViewById(R.id.txtPatientId)
        private var txtDemographics: AppCompatTextView =
            itemView.findViewById(R.id.txtPatientDemographics)

        fun bind(patient: Patient) {
            txtPatientId.text = patient.adminId
//            val demographic = patient.demographics.replace("{", "").replace("}", "")
//                .replace("\"", "").replace(":-:,", "\n").replace(":-:", "")

            val demographic = patient.demographics

            if (demographic.trim().isNotEmpty()) {
                txtDemographics.visibility = View.VISIBLE
                txtDemographics.text = demographic
            } else {
                txtDemographics.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val intent = Intent(activity, SurveyActivity::class.java)
                intent.putExtra(Constants.BundleKey.PATIENT_KEY, patient)
                intent.putExtra(Constants.BundleKey.STUDY_FORM_DETAIL_KEY, formDetail)
                intent.putExtra(Constants.BundleKey.CREATE_STUDY_DATA_KEY, type)
                activity.startActivityForResult(
                    intent,
                    SearchPatientActivity.REQUEST_SEARCH_PATIENT_CODE
                )
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Patient>() {

        override fun areContentsTheSame(
            oldItem: Patient,
            newItem: Patient
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
            return oldItem.id == newItem.id
        }
    }
}