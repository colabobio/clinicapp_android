package org.broadinstitute.clinicapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.studydata.survey.AboutFragment
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

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

    private fun loadModel(path: String) {
        Log.e("TAG", "inside load model method")
        val file = File(path)
        val fileStream = FileInputStream(file)
        getMetadata(fileStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()))
    }

    private fun getMetadata(buffer: MappedByteBuffer) {
        val metadata = MetadataExtractor(buffer)

        val inputSize = metadata.inputTensorCount
        var inputs = emptyArray<String>()

        for (i in 0 until inputSize) {
            val featureNamesTotal = metadata.getInputTensorMetadata(i)?.name()
            val featureNames = featureNamesTotal?.split(", ")

            for (i in 0 until featureNames?.size!!) {
                Log.e("TAG", featureNames[i])
                for (key in variablesAndValue?.keys!!) {
                    if (featureNames[i] == key) {
                        inputs = inputs.plus(variablesAndValue!![key].toString())
                    }
                }
            }
        }

        Log.e("TAG", "outside of loops")

        for (i in 0 until inputs.size) {
            if (inputs[i].equals("Yes")) {
                inputs[i] = "1"
            }

            else if (inputs[i].equals("No")) {
                inputs[i] = "0"
            }

            // Are there any other cases when the values will not be integers?
        }
        tflite = Interpreter(buffer)
        useModel(inputs)
    }

    private fun useModel(inputArray: Array<String>) {
        var inputs = FloatArray(inputArray.size)
        // val mean = 44.88812672413793
        // val standardDeviation = 57.37052911308939

        for (i in 0 until inputs.size) {
            inputs[i] = inputArray[i].toFloat()
        }

        val output = Array(1) {
            FloatArray(1)
        }

//        for(i in 0..7) {
//            input[i] = (((input[i] - mean) / standardDeviation).toFloat())
//        }

//        Log.e("TAG", "all inputs converted")
//        Log.e("TAG", tflite.toString())

        Log.e("TAG", inputs[0].toString())
        Log.e("TAG", inputs[1].toString())
        Log.e("TAG", inputs[2].toString())

        try {
            tflite!!.run(inputs, output)

            if (output[0][0] >= 0.5) {
                diagnosis.text = "Positive Diagnosis"
            }

            else {
                diagnosis.text = "Negative Diagnosis"
            }
        }

        catch (ex: Exception){
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
            diagnosis.text = "Outputted value"
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
}