package org.broadinstitute.clinicapp.ui.studyform.variableselection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables


class VCExpandableAdapter(
    val context: Context,
    private val isReadOnly: Boolean,
    private var categories: List<String>,
    private var selectedVariables: List<Long>,

    private var masterVariablesMap: HashMap<String, List<MasterVariables>>,
    private var onInfoSelectedListener: OnInfoSelectedListener,
    private var onVariableSelectedListener: OnVariableSelectedListener,
    private var callingPage: String
) : BaseExpandableListAdapter() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getGroupCount(): Int {
        return masterVariablesMap.size
    }

    override fun getChildrenCount(parent: Int): Int {
        val list =
            if (masterVariablesMap.size > 1) masterVariablesMap[categories[parent]] else arrayListOf()
        return list?.size ?: 0
    }

    override fun getGroup(parent: Int): Any? {
        return null
    }

    fun setLists(
        map: HashMap<String, List<MasterVariables>>,
        catList: ArrayList<String>,
        list: List<Long>
    ) {
        masterVariablesMap = map
        categories = catList
        selectedVariables = list
        notifyDataSetChanged()
    }

    override fun getChild(parent: Int, child: Int): Any? {
        return null
    }

    override fun getGroupId(parent: Int): Long {
        return 0
    }

    override fun getChildId(parent: Int, child: Int): Long {
        return 0
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    @SuppressLint("InflateParams")
    override fun getGroupView(
        parent: Int,
        isExpanded: Boolean,
        convertView: View?,
        parentView: ViewGroup
    ): View {

        var mView = convertView
        val viewHolderParent: ViewHolderParent
        if (mView == null) {
            mView = inflater.inflate(R.layout.row_variable_category_header, null)
            viewHolderParent = ViewHolderParent()

            viewHolderParent.txtCategory = mView!!.findViewById(R.id.txtVariableCategory)
            mView.tag = viewHolderParent
        } else {
            viewHolderParent = mView.tag as ViewHolderParent
        }

        viewHolderParent.txtCategory!!.text = categories[parent]
        return mView
    }

    override fun getChildView(
        parent: Int,
        child: Int,
        isLastChild: Boolean,
        convertView: View?,
        parentview: ViewGroup
    ): View {
        var mView = convertView

        val viewHolderChild: ViewHolderChild
        val masterVariable = masterVariablesMap[categories[parent]]!![child]
        Log.d("CHILDREN VARIABLES HAVE ARRIVED!!!", masterVariable.toString() )

        if (mView == null) {
            mView = inflater.inflate(R.layout.row_master_variable_child, null)
            viewHolderChild = ViewHolderChild()

            viewHolderChild.chkVariableLabel = mView!!.findViewById(R.id.chkVariableSelected)
            viewHolderChild.imgInfo = mView.findViewById(R.id.imgVariableInfo)
            viewHolderChild.txtVariableLabel = mView.findViewById(R.id.txtVariableLabel)
            mView.tag = viewHolderChild
        } else {
            viewHolderChild = mView.tag as ViewHolderChild
        }

        val spannable: SpannableString
        if(masterVariable.isMandatory){
            spannable = SpannableString(masterVariable.label + " *")
            spannable.setSpan(ForegroundColorSpan(Color.RED),spannable.length-1,spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }else{
            spannable = SpannableString(masterVariable.label)
        }

        if (isReadOnly) {
            viewHolderChild.chkVariableLabel!!.visibility = View.GONE
            viewHolderChild.txtVariableLabel!!.visibility = View.VISIBLE
            viewHolderChild.txtVariableLabel!!.text = spannable
        } else {
            viewHolderChild.txtVariableLabel!!.visibility = View.GONE
            viewHolderChild.chkVariableLabel!!.visibility = View.VISIBLE
            viewHolderChild.chkVariableLabel!!.text = spannable
        }

        viewHolderChild.imgInfo!!.setOnClickListener {
            onInfoSelectedListener.onInfoSelected(
                masterVariable
            )
        }
        viewHolderChild.chkVariableLabel!!.setOnCheckedChangeListener { _, isChecked ->
            Log.d("CHILD", "CHILDREN HAVE BEEN CLICKED!!!" )
            onVariableSelectedListener.onVariableSelected(
                masterVariable,
                isChecked
            )
            Log.d("CHILD MASTER VARIABLE HAS BEEN CHECKED!!!", masterVariable.toString() )
            notifyDataSetChanged()
        }
        if (masterVariable.variableName.equals(
                context.getString(R.string.admin_id),
                ignoreCase = true
            ) && !TextUtils.isEmpty(callingPage) && callingPage == Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
        ) {
            viewHolderChild.chkVariableLabel!!.isChecked = true
        }

        viewHolderChild.chkVariableLabel!!.isEnabled = !masterVariable.variableName.equals(
            context.getString(R.string.admin_id),
            ignoreCase = true
        )
        viewHolderChild.chkVariableLabel!!.isChecked = selectedVariables.contains(masterVariable.id)

        return mView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return false
    }

    private inner class ViewHolderParent {
        internal var txtCategory: AppCompatTextView? = null
    }

    private inner class ViewHolderChild {
        internal var txtVariableLabel: AppCompatTextView? = null
        internal var chkVariableLabel: AppCompatCheckBox? = null
        internal var imgInfo: ImageView? = null
    }
}