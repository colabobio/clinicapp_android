package org.broadinstitute.clinicapp.ui.studyform.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.dao.StudyFormVariablesDao
import org.broadinstitute.clinicapp.util.CommonUtils
import kotlinx.android.synthetic.main.row_study_form_variable.view.*

class DetailsVariableAdapter  constructor( private val mValues: List<StudyFormVariablesDao.StudyFormWithVariable>): RecyclerView.Adapter<DetailsVariableAdapter.ViewHolder>() {

    lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_study_form_variable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mLabelView.text = item.masterVariables.label
        holder.mInfoView.setOnClickListener { CommonUtils.onInfoSelected(item.masterVariables, context) }

        with(holder.mView) {
            tag = item
          //  setOnClickListener{}
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mLabelView: TextView = mView.studyFormDetails_rowVariableTxt
        val mInfoView: ImageView = mView.imgVariableInfo

        override fun toString(): String {
            return super.toString() + " '" + mLabelView.text + "'"
        }
    }
}