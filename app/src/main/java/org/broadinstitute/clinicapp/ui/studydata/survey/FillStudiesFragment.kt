package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_fill_data.view.*
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import java.util.*


class FillStudiesFragment : BaseFragment() {


    private val model: SharedViewModel by activityViewModels()
    private lateinit var txtTitle: AppCompatTextView
    private lateinit var txtFormDetail: AppCompatTextView
    private lateinit var btnNext : ImageButton
    private lateinit var btnPrevious : ImageButton
    private lateinit var btnSkip : Button
    private lateinit var catFrameLayout: LinearLayout
    private lateinit var binCatRadioLayout: LinearLayout
    private lateinit var inputREd: EditText
    private lateinit var tableRow: TableRow
    private lateinit var txtQuestion: AppCompatTextView
    //Date Layout
    private lateinit var dateLayout: LinearLayout
    private lateinit var setDateView : ImageView
    private lateinit var txtSetDate : AppCompatTextView


    private val variableSize by lazy {
        model.list.size
    }

    private val formDetail by lazy {
        model.selected.value
    }

    // Initial current index -1 under show data we increment index
    private var currentIndex = -1
    private var currentTempVariable = ""
    private var currentMasterVariable: MasterVariables? = null
    private val currentCatTag: Int = R.id.fill_catInputLayout


    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_fill_data, container, false).also {
            txtTitle = it.findViewById(R.id.fillData_titleTxt)
            txtFormDetail = it.findViewById(R.id.fill_formDetailsTxt)
            btnNext = it.findViewById(R.id.btn_next)
            btnPrevious = it.findViewById(R.id.btn_previous)
            btnSkip = it.findViewById(R.id.btn_skip)
            catFrameLayout = it.findViewById(R.id.fill_catInputLayout)
            binCatRadioLayout = it.findViewById(R.id.fill_binLayout)
            inputREd = it.findViewById(R.id.fillData_textInputEd)
            dateLayout = it.findViewById(R.id.fill_dateLayout)
            tableRow = it.findViewById(R.id.tableRow)
            txtQuestion = it.findViewById(R.id.fill_quesText)
            setDateView = it.findViewById(R.id.fill_setDateView)
            txtSetDate = it.findViewById(R.id.fill_dateInputTxt)
        }


        //Shows the patient ID information
        val spannable = SpannableString("Patient ID - " + model.patient.value?.adminId)
        spannable.setSpan(RelativeSizeSpan(1f),0,spannable.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.fill_patientTxt.text = spannable

        //if you have more than one variable,
        if(variableSize > 0 ) {
            showData(CallType.Initial)

            //button to go to the next action
            btnNext.setOnClickListener {
                //if this is the last variable
                if (this.currentIndex == variableSize - 1) {
                    if (this.collectValue(CallType.Next)) {
                        //Navigate to the final note on the study form
                        showNoteScreen()
                    }
                }
                // if it is not the last variable
                else if (collectValue(CallType.Next))
                    showData(CallType.Next)
            }

            //if you press the back button
            btnPrevious.setOnClickListener {
                showData(CallType.Previous)
            }

            //if you press the skip button
            btnSkip.setOnClickListener {

                //if this is the last variable
                if (this.currentIndex == variableSize - 1) {
                    //Navigate to the final note on the study form
                    showNoteScreen()

                } else if (collectValue(CallType.Unknown))
                    showData(CallType.Unknown)
            }

            setDateView.setOnClickListener {
                setCurrentDateOnView()
            }

            inputREd.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (currentMasterVariable?.isMandatory!! &&
                        (currentMasterVariable?.type.equals(CategoryType.intType, true)
                                || currentMasterVariable?.type.equals(CategoryType.decType, true)
                                || currentMasterVariable?.type.equals(CategoryType.stringType, true))
                    ) {
                        if (p0?.length!! > 0) {
                            btnNext.isEnabled = true
                            btnNext.setBackgroundResource(R.drawable.button_bg)
                        } else {
                            btnNext.setBackgroundResource(R.drawable.inactive_button_bg)
                            btnNext.isEnabled = false
                        }
                    } else {
                        btnNext.isEnabled = true
                        btnNext.setBackgroundResource(R.drawable.button_bg)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

            })
        }
        return view
    }

    private val binCatListener: RadioGroup.OnCheckedChangeListener
        get() = RadioGroup.OnCheckedChangeListener{group, checkID->

            val checkedRadioButton = group.findViewById(checkID) as RadioButton
            if(checkedRadioButton.isChecked){
                model.variableValues[currentTempVariable] = checkedRadioButton.text.toString()
                if(currentMasterVariable?.isMandatory!!) {
                    btnNext.isEnabled = true
                    btnNext.setBackgroundResource(R.drawable.button_bg)
                }
            }
        }


    @SuppressLint("SetTextI18n")
    private fun showData(callType: CallType){


        when(callType) {
            CallType.Initial -> {
              changeCurrentIndex(true)
            }
            CallType.Next -> {
              changeCurrentIndex(true)
            }
            CallType.Previous -> {
              changeCurrentIndex(false)
            }
            CallType.Unknown -> {
              changeCurrentIndex(true)
            }
        }

        val formWithVariable =  model.list[currentIndex]
        val  variableInfo = formWithVariable.masterVariables
        currentTempVariable = formWithVariable.formVariables.tempStudyFormVariablesId
        currentMasterVariable = variableInfo

        btnSkip.visibility = View.VISIBLE
        btnPrevious.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        inputREd.isEnabled = true // Added due disable when question is patient id
        inputREd.setText("")

        val type = variableInfo.type.toLowerCase(Locale.ENGLISH)
        if(type.equals(CategoryType.stringType,true)
            || type == CategoryType.intType || type == CategoryType.decType){
            inputREd.visibility = View.VISIBLE
        }else {
            // hide keyboard
            context?.let { hideKeyboard(it,inputREd) }
            inputREd.visibility = View.GONE

        }

        catFrameLayout.removeAllViews()
        catFrameLayout.visibility = View.GONE
        binCatRadioLayout.removeAllViews()
        binCatRadioLayout.visibility = View.GONE

        dateLayout.visibility = View.GONE

        var variableValue = model.variableValues[currentTempVariable].toString()
        // For Unknown option we add explicit add . value for this but we don't need populate Unknown field in UI
        if(variableValue == "." || variableValue.isBlank() || variableValue =="null"){
            variableValue = ""
            if (variableInfo.isMandatory) {

                btnNext.isEnabled = false
                btnNext.setBackgroundResource(R.drawable.inactive_button_bg)
            } else {
                btnNext.setBackgroundResource(R.drawable.button_bg)
                btnNext.isEnabled = true
            }
        }

        if(currentIndex == 0){
            btnPrevious.visibility = View.GONE
        }

        txtTitle.text = variableInfo.label
        txtFormDetail.text = formDetail?.masterStudyForms?.title + " - " + variableInfo.variableCategory

        val spannable: SpannableString
        if(variableInfo.isMandatory){
            spannable = SpannableString(getString(R.string.question) + " " +(currentIndex + 1) + "/" + variableSize + " *")
            spannable.setSpan(ForegroundColorSpan(Color.RED),spannable.length-1,spannable.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }else{
            spannable = SpannableString(getString(R.string.question) + " " +(currentIndex + 1) + "/" + variableSize)
        }

        txtQuestion.text  = spannable

        if(variableInfo.variableName.equals(resources.getString(R.string.admin_id),ignoreCase = true)){
            inputREd.setText(model.patient.value?.adminId)
            inputREd.visibility = View.VISIBLE
            inputREd.isEnabled = false
            btnNext.isEnabled = true
            btnNext.setBackgroundResource(R.drawable.button_bg)
            return
        }

         when(type){

            CategoryType.binType ->{
                binCatRadioLayout.visibility = View.VISIBLE
                val viewRadioGroup = RadioGroup(context)
                if(variableInfo.binaryOptions == 1){
                    val y = getString(R.string.yes)
                    val n = getString(R.string.no)
                    addRadioCategoryOptions(y,viewRadioGroup,variableValue.contains(y))
                    addRadioCategoryOptions(n,viewRadioGroup,variableValue.contains(n))

                }else if(variableInfo.binaryOptions == 2){
                    val p = getString(R.string.positive)
                    val n = getString(R.string.negative)
                    addRadioCategoryOptions(p,viewRadioGroup,variableValue.contains(p))
                    addRadioCategoryOptions(n,viewRadioGroup,variableValue.contains(n))
                }

                binCatRadioLayout.addView(viewRadioGroup)

                viewRadioGroup.setOnCheckedChangeListener(binCatListener)

            }
            CategoryType.catType ->{

                catFrameLayout.visibility = View.VISIBLE
                val viewRadioGroup = RadioGroup(context)
                val b = variableInfo.isMultiSelect

                val valuesList = if(variableValue.isNotBlank())variableValue.split("::") else arrayListOf()
                 catFrameLayout.setTag(currentCatTag, valuesList.size)

                if(variableInfo.categoricalOpt1.isNotBlank()){
                    val temp = variableInfo.categoricalOpt1
                   if(b)addCategoryOptions(temp,catFrameLayout,valuesList.contains(temp))
                    else addRadioCategoryOptions(temp,viewRadioGroup,valuesList.contains(temp))
                }
                if(variableInfo.categoricalOpt2.isNotBlank()){
                    val temp = variableInfo.categoricalOpt2
                    if(b)addCategoryOptions(temp, catFrameLayout,valuesList.contains(temp))
                    else addRadioCategoryOptions(temp,viewRadioGroup, valuesList.contains(temp))
                }
                if(variableInfo.categoricalOpt3.isNotBlank()){
                    val temp = variableInfo.categoricalOpt3
                    if(b)addCategoryOptions(temp, catFrameLayout,valuesList.contains(temp))
                    else addRadioCategoryOptions(temp,viewRadioGroup, valuesList.contains(temp))
                }
                if(variableInfo.categoricalOpt4.isNotBlank()){
                    val temp = variableInfo.categoricalOpt4
                    if(b)addCategoryOptions(temp,catFrameLayout,valuesList.contains(temp))
                    else addRadioCategoryOptions(temp,viewRadioGroup, valuesList.contains(temp))
                }
                if(variableInfo.categoricalOpt5.isNotBlank()){
                    val temp = variableInfo.categoricalOpt5
                    if(b)addCategoryOptions(temp,catFrameLayout,valuesList.contains(temp))
                    else addRadioCategoryOptions(temp,viewRadioGroup, valuesList.contains(temp))
                }

                if(!b){
                    viewRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                        val checkedRadioButton = group.findViewById(checkedId) as RadioButton
                        val isChecked = checkedRadioButton.isChecked
                        if(isChecked){
                            model.variableValues[currentTempVariable] = checkedRadioButton.text.toString()
                            if(currentMasterVariable?.isMandatory!!) {
                                btnNext.isEnabled = true
                                btnNext.setBackgroundResource(R.drawable.button_bg)
                            }
                        }
                    }
                }

                catFrameLayout.addView(viewRadioGroup)
            }
            CategoryType.dateType ->{
                dateLayout.visibility = View.VISIBLE

                if(variableValue.isBlank()){
                    val cal = Calendar.getInstance()
                    val year = cal.get(Calendar.YEAR)
                    val month = cal.get(Calendar.MONTH)
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    txtSetDate.text = StringBuilder().append(month + 1)
                        .append("/").append(day).append("/").append(year)
                        .append(" ")
                }else {
                    txtSetDate.text = variableValue
                }

                btnNext.setBackgroundResource(R.drawable.button_bg)
                btnNext.isEnabled = true

            }
            CategoryType.intType ->{
                inputREd.visibility = View.VISIBLE
                inputREd.maxLines = 1
                inputREd.setText(variableValue)
                inputREd.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_SIGNED//for decimal numbers

            }
            CategoryType.decType ->{
                inputREd.visibility = View.VISIBLE
                inputREd.maxLines = 1
                inputREd.setText(variableValue)
                inputREd.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

            }
            CategoryType.stringType ->{
                inputREd.visibility = View.VISIBLE
                inputREd.setText(variableValue)
                inputREd.maxLines = 10
                inputREd.inputType = InputType.TYPE_CLASS_TEXT  + InputType.TYPE_TEXT_FLAG_MULTI_LINE

            }
        }
    }

    private fun addCategoryOptions(text : String, view :LinearLayout, isChecked: Boolean){
        val space: Int = resources.getDimension(R.dimen.dimen_10).toInt()
        val c = CheckBox(context)
        c.text = text
        c.id = View.generateViewId()
        c.setPadding(space,0,space,space)
        c.isChecked = isChecked

        c.setOnCheckedChangeListener { _, p1 ->

            if(currentMasterVariable?.isMandatory!! && currentMasterVariable?.type.equals(CategoryType.catType, true)){
                val tagValue = catFrameLayout.getTag(currentCatTag)
                if(p1){
                    if(tagValue is Int){
                        catFrameLayout.setTag(currentCatTag, tagValue + 1)
                    }
                    btnNext.setBackgroundResource(R.drawable.button_bg)
                    btnNext.isEnabled = true
                }else {
                    if(tagValue is Int){
                        val v = tagValue - 1
                        catFrameLayout.setTag(currentCatTag, v )
                        if(v < 1){
                            btnNext.setBackgroundResource(R.drawable.inactive_button_bg)
                            btnNext.isEnabled = false
                        }
                    }
                }
            }
        }
        view.addView(c)
    }

    private fun addRadioCategoryOptions(text: String, view: RadioGroup, contains: Boolean){
        val space: Int = resources.getDimension(R.dimen.dimen_10).toInt()
        val c  = RadioButton(context)
        c.text = text
        c.id = View.generateViewId()
        c.setPadding(space,0,space,space)
        c.isChecked = contains
        view.addView(c)
    }

    private fun changeCurrentIndex( isIncrement: Boolean ){

        if(isIncrement && currentIndex < variableSize - 1){
            currentIndex++
        } else if(!isIncrement && currentIndex > 0) currentIndex--

    }

    private fun collectValue(callType: CallType) : Boolean{

        if(currentMasterVariable !=null && callType != CallType.Unknown) {
            var variableValue = ""
            when(currentMasterVariable?.type?.toLowerCase(Locale.ENGLISH)){

                CategoryType.binType ->{
                    // We set value in onChanged listener
                    variableValue =  model.variableValues[currentTempVariable].toString()
                }
                CategoryType.catType ->{

                    //if current variable is a multiselection
                    if(!currentMasterVariable!!.isMultiSelect){
                        val radioGroup = catFrameLayout.getChildAt(0)
                       if(radioGroup is RadioGroup) {
                           val selectedId = radioGroup.checkedRadioButtonId
                           val selectRadio = radioGroup.findViewById<RadioButton>(selectedId)
                           variableValue = (selectRadio?.text?.toString() ?: "")
                       }
                    } else {
                        val count = catFrameLayout.childCount
                        var checkBoxChoices = ""
                        for (x in 0 until count){
                            val checkBox = catFrameLayout.getChildAt(x)
                            if(checkBox is CheckBox){
                                if(checkBox.isChecked)
                                    if(checkBoxChoices.isEmpty()){
                                        checkBoxChoices = checkBox.text.toString()
                                    }else {
                                        checkBoxChoices += "::"+checkBox.text.toString()
                                    }
                            }
                        }
                        variableValue = checkBoxChoices
                    }
                }
                CategoryType.dateType ->{
                    variableValue = txtSetDate.text.toString().trim()
                }
                CategoryType.intType , CategoryType.decType , CategoryType.stringType ->{
                    variableValue =  inputREd.text.toString().trim()
                }

            }
            model.variableValues[currentTempVariable] = variableValue
            return if(currentMasterVariable?.isMandatory!!) {
                if(variableValue.isNotBlank()) true
                else {
                    onError(getString(R.string.provide_inputs))
                    false
                }
            } else true
        } else {
            model.variableValues[currentTempVariable] = "."
        }
        return true

    }

    private fun showNoteScreen(){

       if(inputREd.isVisible) context?.let { hideKeyboard(it,inputREd) }
        val transaction = this.activity?.supportFragmentManager?.beginTransaction()
        transaction?.addToBackStack("note")
        transaction?.replace(R.id.flAddMoreVars, NoteFragment.newInstance(), "note")
        transaction?.commit()
    }


    private val datePickerListener =
        DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
            // when dialog box is closed, below method will be called.
            txtSetDate.text = StringBuilder().append(selectedMonth + 1)
                    .append("/").append(selectedDay).append("/").append(selectedYear)
                    .append(" ")

        }

    private fun setCurrentDateOnView() {

        val variableValue = model.variableValues[currentTempVariable]
        val valuesList = variableValue?.split("/") ?: arrayListOf()
        val cal = Calendar.getInstance()
        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH)
        var day = cal.get(Calendar.DAY_OF_MONTH)


        if(valuesList.size > 1){
                year = valuesList[2].toInt()
                day = valuesList[1].toInt()
                month = valuesList [0].toInt()-1
          }

        val datePicker = activity?.let {
            DatePickerDialog(
                it,
                R.style.AppTheme, datePickerListener,
               year,
                month,
                day)
        }


        datePicker?.show()
        datePicker?.setCancelable(false)
        datePicker?.setTitle("Select the date")

    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment DetailsFragment.
         */
        @JvmStatic
        fun newInstance() =
            FillStudiesFragment().apply {}

    }

    // int= integer, cat=categorical, dec= continuous decimal, string=text, bin , date
    object CategoryType{
        const val binType = "bin"
        const val intType = "int"
        const val decType = "dec"
        const val catType = "cat"
        const val stringType = "string"
        const val dateType = "date"

    }

    enum class CallType{
        Next,
        Unknown,
        Previous,
        Initial
    }
}