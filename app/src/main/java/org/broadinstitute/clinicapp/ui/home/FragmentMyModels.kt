package org.broadinstitute.clinicapp.ui.home

import ModelListAdapter
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.broadinstitute.clinicapp.R
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.sql.Timestamp
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.pop_create_studyform.view.*
import kotlinx.android.synthetic.main.pop_import_model.view.*
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.ui.studyform.CreateFormActivity

class FragmentMyModels : Fragment(), CoroutineScope by MainScope() {

    private val TAG = "MainActivity"
    private var client: GoogleSignInClient? = null

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2

    private var mDriveServiceHelper: DriveServiceHelper? = null

    private lateinit var googleDriveService: Drive

    // encoded machine learning model
    /*var tflite: Interpreter? = null
    var pregnancies: EditText? = null
    var glucose: EditText? = null
    var bloodPressure: EditText? = null
    var skinThickness: EditText? = null
    var insulin: EditText? = null
    var BMI: EditText? = null
    var diabetesPedigree: EditText? = null
    var age: EditText? = null
    var button: Button? = null
    var output: TextView? = null*/

    private lateinit var rvModels: RecyclerView
    private lateinit var fabModels: FloatingActionButton

    private val itemsList = ArrayList<String>()
    private lateinit var modelListAdapter: ModelListAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_models, container, false)
//        view.findViewById<View>(R.id.open_btn).setOnClickListener{ openFilePicker() }
//        view.findViewById<View>(R.id.sign_in_btn).setOnClickListener{ requestSignIn() }
//        view.findViewById<View>(R.id.log_out_btn).setOnClickListener{ logout() }

        /*button?.setOnClickListener {
            buttonClicked()
        }*/
        rvModels = view.findViewById(R.id.rvModels)

        fabModels = view.findViewById(R.id.fabModels)
        Log.e("TAG", fabModels.toString())
        fabModels.setOnClickListener {
            showCreationFormDialog()
        }

        // ended coding here
        val linearLayoutManager = LinearLayoutManager(context)
        modelListAdapter = ModelListAdapter(itemsList)
        rvModels.layoutManager = linearLayoutManager
        rvModels.adapter = modelListAdapter

        requestSignIn()
        findIItems()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == AppCompatActivity.RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }
            REQUEST_CODE_OPEN_DOCUMENT -> if (resultCode == AppCompatActivity.RESULT_OK && resultData != null) {
                val uri = resultData.data
                uri?.let { openFileFromFilePicker(it) }
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun requestSignIn() {
        Log.d(TAG, "Requesting sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()
        client = GoogleSignIn.getClient(requireActivity(), signInOptions)

        startActivityForResult(client!!.signInIntent, REQUEST_CODE_SIGN_IN)
    }


    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Log.d(TAG, "Signed in as " + googleAccount.email)

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireActivity(), Collections.singleton(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account
                googleDriveService =
                    Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        GsonFactory(),
                        credential
                    )
                        .setApplicationName("Drive API Migration")
                        .build()

                // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                // Its instantiation is required before handling any onClick actions.
                mDriveServiceHelper = DriveServiceHelper()
            }

            .addOnFailureListener { exception: java.lang.Exception? ->
                Log.e(
                    TAG,
                    "Unable to sign in.",
                    exception
                )
            }
    }

    private fun openFilePicker() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening file picker.")
            val pickerIntent = mDriveServiceHelper!!.createFilePickerIntent()

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent!!, REQUEST_CODE_OPEN_DOCUMENT)
        }
    }

    private fun openFileFromFilePicker(uri: Uri) {

        lateinit var URIName: String
        lateinit var URITime: String

        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening " + uri.path)
            mDriveServiceHelper!!.openFileUsingStorageAccessFramework(requireActivity().contentResolver, uri)
                ?.addOnSuccessListener { nameAndTime: Pair<String?, String?>? ->
                    URIName = nameAndTime!!.first.toString()
                    URITime = nameAndTime!!.second.toString()
                }
                ?.addOnFailureListener { exception: java.lang.Exception? ->
                    Log.e(
                        TAG,
                        "Unable to open file from picker.",
                        exception
                    )
                }
        }

        launch(Dispatchers.Default) {
            var pageToken: String? = null
            do {
                val result = googleDriveService.files().list().apply {
                    q = "mimeType='application/octet-stream'"
                    fields = "nextPageToken, files(id, name, modifiedTime)"
                    this.pageToken = pageToken
                }.execute()
                for (file in result.files) {
                    val fileName = file.name
                    val fileModifiedTime = file.modifiedTime.toString().substring(0,23)

                    val stamp = Timestamp(URITime.toLong())
                    var dateTime = DateTime(stamp.time)

                    val dateTimeHours = dateTime.toString().substring(11,13).toInt()
                    var hoursTimeZone: String

                    if (dateTimeHours > 20) {
                        hoursTimeZone = if (dateTimeHours == 21) {
                            "01"
                        } else if (dateTimeHours == 22) {
                            "02"
                        } else if (dateTimeHours == 23) {
                            "03"
                        } else {
                            "04"
                        }
                    }

                    else {
                        hoursTimeZone = (dateTime.toString().substring(11,13).toInt() + 4).toString()
                    }

                    val dateTimeString = dateTime.toString().substring(0,11) + hoursTimeZone + dateTime.toString().substring(13,23)

                    if (fileName.compareTo(URIName) == 0 && dateTimeString.compareTo(fileModifiedTime) == 0) {
                        val fileid = file.id

                        val fileNameLength = fileName.length
                        if (fileName.substring(fileNameLength - 7).compareTo(".tflite") == 0) {
                            val finalFile = File(context?.filesDir, "$fileName")

                            val out: OutputStream = FileOutputStream(finalFile)
                            googleDriveService.files().get(fileid)
                                .executeMediaAndDownloadTo(out)

                            Log.e("TAG", "the file was downloaded!")
                            itemsList.add(fileName)
                            //modelListAdapter.notifyDataSetChanged()

                        }
                    }
                }
            } while (pageToken != null)
        }
    }

    private fun logout() {

        if (client == null) {
            return
        }
        else {
            client!!.signOut()
        }
    }

   /* private fun loadModel(path: String): MappedByteBuffer? {
        Log.e("TAG", "file path was changed")
        val file = File(path)
        val fileStream = FileInputStream(file)
        return fileStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
    }*/

  /*  private fun useModel(
        pregnancies: String,
        glucose: String,
        bloodPressure: String,
        skinThickness: String,
        insulin: String,
        BMI: String,
        diabetesPedigree: String,
        age: String
    ): String? {
        val input = FloatArray(8)
        val mean = 44.88812672413793
        val standardDeviation = 57.37052911308939

        input[0] = pregnancies.toFloat()
        input[1] = glucose.toFloat()
        input[2] = bloodPressure.toFloat()
        input[3] = skinThickness.toFloat()
        input[4] = insulin.toFloat()
        input[5] = BMI.toFloat()
        input[6] = diabetesPedigree.toFloat()
        input[7] = age.toFloat()

        val output = Array(1) {
            FloatArray(1)
        }

        for(i in 0..7) {
            input[i] = (((input[i] - mean) / standardDeviation).toFloat())
        }

        Log.e("TAG", "all inputs converted")
        Log.e("TAG", tflite.toString())
        try {
            tflite!!.run(input, output)

            if (output[0][0] >= 0.5) {
                return "Diabetes detected"
            }

            else {
                return "Diabetes not detected"
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


        }

        return "Outputted value"
    }
*/
    private fun findIItems() {

        try {

            // var selectedFileIndex = 0
            var fileNames = emptyArray<String>()
            var filePaths = emptyArray<String>()

            val path = "/data/data/org.broadinstitute.clinicapp/files"
            val directory = File(path)
            val currentFiles = directory.listFiles()

            for (file in currentFiles) {

                val filePath = file.path

                if (filePath.compareTo("/data/data/org.broadinstitute.clinicapp/files/secretPass.txt") == 0) {
                    continue
                }
                val fullName = file.path.substringAfterLast("/")
                val fileName = fullName.substringBeforeLast(".")

                filePaths = filePaths.plus(filePath)
                fileNames = fileNames.plus(fileName)
                itemsList.add(fileName)
                modelListAdapter.notifyDataSetChanged()

            }
        }

        catch(ex: java.lang.Exception) {
        Log.e("TAG", "something went wrong")}
  }

           /* var selectedFilePath = filePaths[0]

            MaterialAlertDialogBuilder(requireActivity())
                .setTitle("Choose a model")
                .setSingleChoiceItems(fileNames, selectedFileIndex) {dialog, which ->
                    selectedFileIndex = which
                    selectedFilePath = filePaths[which]
                    Log.e("TAG", selectedFilePath)
                }
                .setPositiveButton("OK") { dialog, which ->

                    try {
                        tflite = loadModel(selectedFilePath)?.let { Interpreter(it) }
                    } catch (ex: Exception) {
                        print("exception produced")
                    }

                    Log.e("TAG", "over here before nulls checked")

                    try {
                        val predicted_y = useModel(
                            pregnancies?.getText().toString(),
                            glucose?.getText().toString(),
                            bloodPressure?.getText().toString(),
                            skinThickness?.getText().toString(),
                            insulin?.getText().toString(),
                            BMI?.getText().toString(),
                            diabetesPedigree?.getText().toString(),
                            age?.getText().toString()
                        )
                        output?.setText(predicted_y)
                    }

                    catch (ex: Exception) {
                        val dialogBuilder = AlertDialog.Builder(requireActivity())

                        // set message of alert dialog
                        dialogBuilder.setMessage("One or more parameters have not been entered correctly. Please make sure all inputs are present and numerical values.")
                            .setCancelable(true)

                        // create dialog box
                        val alert = dialogBuilder.create()
                        // set title for alert dialog box
                        alert.setTitle("Incorrect inputs.")
                        // show alert dialog
                        alert.show()
                    }
                }

                .setNeutralButton("CANCEL") {dialog, which ->

                }.show()
        }

        catch (ex: Exception) {
            Log.e("TAG", ex.toString())
            val dialogBuilder = AlertDialog.Builder(requireActivity())

            // set message of alert dialog
            dialogBuilder.setMessage("No models have been downloaded. Please download a model from your Google Drive.")
                .setCancelable(true)

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("No models found.")
            // show alert dialog
            alert.show()
        }
    }*/

    private fun showCreationFormDialog() {

        val mDialogView = LayoutInflater.from(context).inflate(R.layout.pop_import_model, null)
        val mBuilder = android.app.AlertDialog.Builder(context)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.pop_google_drive.setOnClickListener {
            mAlertDialog.dismiss()
            openFilePicker()
        }
        //cancel button click of custom layout
        mDialogView.pop_other_source.setOnClickListener {
            mAlertDialog.dismiss()

            val dialogBuilder = AlertDialog.Builder(requireActivity())

            // set message of alert dialog
            dialogBuilder.setMessage("Will be used if another source is added.")
                .setCancelable(true)

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Other Source")
            // show alert dialog
            alert.show()
        }

        mDialogView.pop_other_source2.setOnClickListener {
            mAlertDialog.dismiss()

            val dialogBuilder = AlertDialog.Builder(requireActivity())

            // set message of alert dialog
            dialogBuilder.setMessage("Will be used if another source is added.")
                .setCancelable(true)

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Other Source 2")
            // show alert dialog
            alert.show()
        }
    }

}