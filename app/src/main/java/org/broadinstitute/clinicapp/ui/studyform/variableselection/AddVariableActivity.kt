package org.broadinstitute.clinicapp.ui.studyform.variableselection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_add_variable.*
import org.broadinstitute.clinicapp.R

class AddVariableActivity : AppCompatActivity() {
    private var variable_id: TextView? = null
    private var variable_category: EditText? = null
    private var variable_name: EditText? = null
    private var variable_label: EditText? = null
    private var variable_description: EditText? = null

    var mandatoryValue: Int = 0

    var variableChoiceTypeId: Int = 0

//    android:id="@+id/first_choice"
//    android:layout_width="wrap_content"
//    android:layout_height="wrap_content"
//    android:hint="Enter singly selectable choice"
//    android:textSize="@dimen/dimen_15" />

    //    private var radio_mandatory_yes: EditText? = null
//    private var radio_mandatory_no: EditText? = null
    private var variable_type: EditText? = null

//    private var radio_binary: RadioButton? = null
//    private var radio_int: RadioButton? = null
//    private var radio_cat: RadioButton? = null
//    private var radio_date: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_variable)

        variable_id = findViewById(R.id.variable_id)
        variable_category = findViewById(R.id.variable_category)
        variable_name = findViewById(R.id.variable_name)
        variable_label = findViewById(R.id.variable_label)
        variable_description = findViewById(R.id.variable_description)

//        radio_mandatory_yes = findViewById(R.id.radio_mandatory_yes)
//        radio_mandatory_no = findViewById(R.id.radio_mandatory_no)

//        radio_binary = findViewById(R.id.radio_binary)
//        radio_int = findViewById(R.id.radio_int)
//        radio_cat = findViewById(R.id.radio_cat)
//        radio_date = findViewById(R.id.radio_date)

        radio_mandatory_yes.setOnClickListener{
            mandatoryValue = 1
            Log.d("mandatoryValue is", mandatoryValue.toString())
        }

        radio_mandatory_no.setOnClickListener{
            mandatoryValue = 0
            Log.d("mandatoryValue is", mandatoryValue.toString())
        }

        radio_single.setOnClickListener{
            Log.d("Radio", "radio binary clicked")
            single_variable_options.visibility = View.VISIBLE

        }

        radio_int.setOnClickListener{
            single_variable_options.visibility = View.GONE

        }

        radio_cat.setOnClickListener{
            single_variable_options.visibility = View.VISIBLE

        }

        radio_date.setOnClickListener{
            single_variable_options.visibility = View.GONE

        }

        type_options.setOnClickListener{
            val parentLayout = type_options.parent as ViewGroup
            val editTextView = EditText(this)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            editTextView.layoutParams = params
            editTextView.hint = "Enter choice"
            var variableID = "${variableChoiceTypeId}_choice"
            editTextView.id = variableChoiceTypeId++

            Log.d("textView.id", editTextView.id.toString() )

            single_variable_options.addView(editTextView)
        }




    }

}