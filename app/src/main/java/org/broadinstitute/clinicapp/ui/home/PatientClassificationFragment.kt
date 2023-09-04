package org.broadinstitute.clinicapp.ui.home

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.survey.AboutFragment
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.reflect.typeOf

class PatientClassificationFragment : Fragment() {
    private var studyForMDetail: StudyFormDetail? = null
    private var variablesAndValue: LinkedHashMap<String, String>? = null
    private var tflite: Interpreter? = null
    lateinit var diagnosis:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            studyForMDetail = it.getParcelable("studyForm") as StudyFormDetail?
            variablesAndValue = it.getSerializable("variablesAndValues") as? LinkedHashMap<String, String>

        }

        println("studyForMDetail in Models = $studyForMDetail")
        println("variablesAndValue in Models = $variablesAndValue")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_patient_classification, container, false)
        diagnosis = view.findViewById(R.id.diagnosis)

        loadModel(AboutFragment.chosenPath)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadModel(path: String) {
        Log.e("TAG", "inside load model method")
        val file = File(path)
        val fileStream = FileInputStream(file)
        getMetadata(fileStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMetadata(buffer: MappedByteBuffer) {
        val metadata = MetadataExtractor(buffer)
        var totalFeatureNames: List<String> = listOf()

        val inputSize = metadata.inputTensorCount
        var inputs = emptyArray<String>()
        var variableCounter = 0

        for (i in 0 until inputSize) {
            val featureNamesTotal = metadata.getInputTensorMetadata(i)?.name()
            val featureNames = featureNamesTotal?.split(", ")

            for (j in 0 until featureNames?.size!!) {
                Log.e("TAG", "OVER HEREEEEEEE")
                totalFeatureNames = totalFeatureNames.plus(featureNames[j])
                variableCounter = 0

                for (key in variablesAndValue?.keys!!) {
                    if (featureNames[j] == key) {
                        inputs = inputs.plus(variablesAndValue!![key].toString())
                    }

                   else {
                        variableCounter +=1
                    }

                    if (variableCounter == variablesAndValue!!.keys.size) {
                        val dialogBuilder = AlertDialog.Builder(requireActivity())

                        // set message of alert dialog
                        dialogBuilder.setMessage("The model inputs are not recognized. Please make sure the model's features correspond to a variable from our list.")
                            .setCancelable(true)

                        // create dialog box
                        val alert = dialogBuilder.create()
                        // set title for alert dialog box
                        alert.setTitle("Model inputs not recognized.")
                        // show alert dialog
                        alert.show()
                        diagnosis.text = "Model inputs not recognized, no diagnosis generated."
                    }
                }


            }
        }

        Log.e("TAG", "outside of loops")

        val jsonString: String
        val fileName = "variableListData.json"
        try {
            jsonString = context?.assets?.open(fileName)?.bufferedReader().use { it?.readText() ?: String() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return
        }

        Log.e("TAG", jsonString)

        val gson = Gson()
        val listDataType = object : TypeToken<List<VariableData>>() {}.type

        var variableDataList: List<VariableData> = gson.fromJson(jsonString, listDataType)
        variableDataList.forEachIndexed { idx, data -> Log.i("data", "> Item $idx:\n$data") }

        var dataListLength = variableDataList.size
        var finalInputs = mutableListOf<Float>()

        for (i in inputs.indices) {
            for (j in 0 until dataListLength) {
                if(totalFeatureNames[i] == variableDataList[j].variableName) {
                    val variable = variableDataList[j]

                    if (variable.type == "int" || variable.type == "dec") {
                        finalInputs.add(inputs[i].toFloat())
                    }

                    if (variable.type == "bin") {
                        if (inputs[i] == "Yes") {
                            Log.e("TAG", variable.toString())
                            finalInputs.add(1.0F)
                        }

                        else if (inputs[i] == "No") {
                            finalInputs.add(0.0F)
                        }
                    }

                    if (variable.type == "cat") {
                        if (inputs[i] == variable.categoricalOpt1) {
                            var oneHotEncoded = arrayListOf<Float>(1.0F,0.0F,0.0F,0.0F,0.0F)
                            finalInputs = (finalInputs + oneHotEncoded) as MutableList<Float>
                        }

                        else if (inputs[i] == variable.categoricalOpt2) {
                            var oneHotEncoded = arrayListOf<Float>(0.0F,1.0F,0.0F,0.0F,0.0F)
                            finalInputs = (finalInputs + oneHotEncoded) as MutableList<Float>
                        }

                        else if (inputs[i] == variable.categoricalOpt3) {
                            var oneHotEncoded = arrayListOf<Float>(0.0F,0.0F,1.0F,0.0F,0.0F)
                            finalInputs = (finalInputs + oneHotEncoded) as MutableList<Float>
                        }

                        else if (inputs[i] == variable.categoricalOpt4) {
                            var oneHotEncoded = arrayListOf<Float>(0.0F,0.0F,0.0F,1.0F,0.0F)
                            finalInputs = (finalInputs + oneHotEncoded) as MutableList<Float>
                        }

                        else {
                            var oneHotEncoded = arrayListOf<Float>(0.0F,0.0F,0.0F,0.0F,1.0F)
                            finalInputs = (finalInputs + oneHotEncoded) as MutableList<Float>
                        }
                    }
                }
            }
        }

        tflite = Interpreter(buffer)
        Log.e("TAG", finalInputs.toString())
        Log.e("TAG", finalInputs.size.toString())

        for(i in 0 until finalInputs.size) {
            val value = finalInputs[i]
            println(value::class.java.typeName)
        }

        useModel(finalInputs)

    }

    private fun useModel(inputList: List<Float>) {
        var inputs = FloatArray(inputList.size)
        for (i in 0 until inputs.size) {
            inputs[i] = inputList[i]
            Log.e("TAG", inputs.size.toString())
            Log.e("TAG", inputs[i].toString())
      }

        val output = Array(1) {
            FloatArray(1)
        }

        try {
            tflite!!.run(inputs, output)

            if (output[0][0] >= 0.5) {
                diagnosis.text = output[0][0].toString()
            }

            else {
                diagnosis.text = output[0][0].toString()
            }
        }

        catch (ex: Exception){
            Log.e("TAG", ex.toString())
            val dialogBuilder = AlertDialog.Builder(requireActivity())

            // set message of alert dialog
            dialogBuilder.setMessage("The model you have chosen does not work for the data in this study.")
                .setCancelable(true)

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Wrong model chosen.")
            // show alert dialog
            alert.show()
            diagnosis.text = "Wrong model chosen, no diagnosis generated."
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PatientClassificationFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(studyForMDetail: StudyFormDetail?, variablesAndValue: LinkedHashMap<String, String>) =
            PatientClassificationFragment().apply {
                println("variablesAndValue in newInstance For Models = $variablesAndValue")
                arguments = Bundle().apply {
                    putParcelable("studyForm", studyForMDetail)
                    putSerializable("variablesAndValues", variablesAndValue)
                }
            }
    }

    data class VariableData(val variableName: String, val type: String, val categoricalOpt1: String, val categoricalOpt2: String, val categoricalOpt3: String, val categoricalOpt4: String, val categoricalOpt5: String){}
}