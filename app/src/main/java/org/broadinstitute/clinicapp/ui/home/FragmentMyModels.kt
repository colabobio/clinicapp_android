package org.broadinstitute.clinicapp.ui.home

import ModelListAdapter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.android.synthetic.main.pop_import_model.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.tensorflow.lite.support.metadata.MetadataExtractor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.sql.Timestamp
import java.util.*
import java.time.ZoneId

class FragmentMyModels : Fragment(), CoroutineScope by MainScope() {

    private val TAG = "MainActivity"

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2

    private var mDriveServiceHelper: DriveServiceHelper? = null

    private lateinit var googleDriveService: Drive

    private lateinit var rvModels: RecyclerView
    private lateinit var fabModels: FloatingActionButton

    private var itemsList = ArrayList<String>()
    private lateinit var modelListAdapter: ModelListAdapter

    companion object {
        var fileNames = emptyArray<String>()
        var fileFullNames = emptyArray<String>()
        var filePaths = emptyArray<String>()
        var email = String()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_models, container, false)
        rvModels = view.findViewById(R.id.rvModels)

        fabModels = view.findViewById(R.id.fabModels)
        fabModels.setOnClickListener {
            showCreationFormDialog()
        }

        val linearLayoutManager = LinearLayoutManager(context)
        modelListAdapter = ModelListAdapter(itemsList)
        rvModels.layoutManager = linearLayoutManager
        rvModels.adapter = modelListAdapter

        startActivityForResult(LoginActivity.client!!.signInIntent, REQUEST_CODE_SIGN_IN)

        findIItems()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    private fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleAccount: GoogleSignInAccount ->
                Log.d(TAG, "Signed in as " + googleAccount.email)

                // Use the authenticated account to sign in to the Drive service.
                val credential = GoogleAccountCredential.usingOAuth2(
                    requireActivity(), Collections.singleton(DriveScopes.DRIVE_FILE)
                )
                credential.selectedAccount = googleAccount.account
                email = googleAccount.email.toString()
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
                findIItems()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openFileFromFilePicker(uri: Uri) {

        lateinit var URIName: String
        lateinit var URITime: String

        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Opening " + uri.path)
            mDriveServiceHelper!!.openFileUsingStorageAccessFramework(
                requireActivity().contentResolver,
                uri
            )
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
                    val fileModifiedTime = file.modifiedTime.toString().substring(0, 23)

                    val stamp = Timestamp(URITime.toLong())
                    var dateTime = DateTime(stamp.time)
                    
                    Log.e("TAG", dateTime.toString())
                    val timeZoneShift = dateTime.toString().substring(24,26).toInt()
                    val lateNight = 24 - timeZoneShift
                    Log.e("TAG", timeZoneShift.toString())

                    val dateTimeHours = dateTime.toString().substring(11, 13).toInt()
                    var dateDay = dateTime.toString().substring(8, 10).toInt()
                    var hoursTimeZone: String
                    var dateDayString: String

                    if (dateTimeHours >= lateNight) {
                        if (dateTimeHours + timeZoneShift == 24) {
                            hoursTimeZone = "00"
                            dateDay = dateDay.toInt() + 1
                        }
                        else if (dateTimeHours + timeZoneShift == 25) {
                            hoursTimeZone = "01"
                            dateDay = dateDay.toInt() + 1
                        }
                        else if (dateTimeHours + timeZoneShift == 26) {
                            hoursTimeZone = "02"
                            dateDay = dateDay.toInt() + 1

                        } else if (dateTimeHours + timeZoneShift == 27) {
                            hoursTimeZone = "03"
                            dateDay = dateDay.toInt() + 1
                        }
                        else {
                            hoursTimeZone = "04"
                            dateDay = dateDay.toInt() + 1
                        }
                    }

                    else if (dateTimeHours >= 0 && dateTimeHours < 10 - timeZoneShift && timeZoneShift < 10) {
                        hoursTimeZone = "0" + (dateTimeHours + timeZoneShift).toString()
                    }

                    else {
                        hoursTimeZone =
                            (dateTime.toString().substring(11, 13).toInt() + timeZoneShift).toString()
                    }

                    if(dateDay in 0..9) {
                        dateDayString = "0$dateDay"
                    }

                    else {
                        dateDayString = dateDay.toString()
                    }

                    val dateTimeString =
                        dateTime.toString().substring(0, 8) + dateDayString + "T" + hoursTimeZone + dateTime.toString()
                            .substring(13, 23)

                    Log.e("TAG", fileName)
                    Log.e("TAG", URIName)
                    Log.e("TAG", dateTimeString)
                    Log.e("TAG", fileModifiedTime)

                    if (fileName.compareTo(URIName) == 0 && dateTimeString.compareTo(
                            fileModifiedTime
                        ) == 0
                    ) {
                        val fileid = file.id

                        val fullFileName = "$email $fileName"
                        val fileType = ".tflite"
                        if (fileType in fileName) {
                            val finalFile = File(context?.filesDir, "$fullFileName")

                            val out: OutputStream = FileOutputStream(finalFile)
                            googleDriveService.files().get(fileid)
                                .executeMediaAndDownloadTo(out)
                            Log.e("TAG", "the file was downloaded!")

                            launch(Dispatchers.Main) {
                                if (loadModel(finalFile.absolutePath)) {
                                    findIItems()
                                }

                                else {
                                    finalFile.delete()
                                }
                            }
                        }

                        else {
                            launch(Dispatchers.Main) {
                                val dialogBuilder = AlertDialog.Builder(requireActivity())
                                // set message of alert dialog
                                dialogBuilder.setMessage("Please make sure to only import .tflite files from your drive. Other file types are not supported..")
                                    .setCancelable(true)
                                // create dialog box
                                val alert = dialogBuilder.create()
                                // set title for alert dialog box
                                alert.setTitle("Only tflite models can be downloaded.")
                                // show alert dialog
                                alert.show()
                            }
                        }
                    }
                }
            } while (pageToken != null)
        }
    }

    private fun findIItems() {
        try {

        val path = context?.filesDir?.absolutePath
        val directory = File(path)
        val currentFiles = directory.listFiles()

        for (file in currentFiles) {
            val filePath = file.path

            if (filePath.compareTo(path + "/secretPass.txt") == 0) {
                continue
            }
            val fullName = file.path.substringAfterLast("/")
            val fileName = fullName.substringBeforeLast(".").substringAfter(" ")
            val fileOwner = file.path.substringAfter("/files/").substringBefore(" ")

            if (fileOwner.compareTo(email) == 0 && fullName !in fileFullNames)
            {
                fileFullNames = fileFullNames.plus(fullName)
                filePaths = filePaths.plus(filePath)
                fileNames = fileNames.plus(fileName)
                itemsList.add(fileName)
            }
        }

        launch (Dispatchers.Main){
            // Stuff that updates the UI
            modelListAdapter.notifyDataSetChanged()
        }
    } catch(ex: java.lang.Exception) {
        Log.e("TAG", ex.toString())
    }
  }

    private fun loadModel(path: String): Boolean {
        Log.e("TAG", "inside load model method")
        val file = File(path)
        val fileStream = FileInputStream(file)
        if(getMetadata(fileStream.channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()))) {
            return true
        }

        return false
    }
    
    private fun getMetadata(buffer: MappedByteBuffer): Boolean {
        val metadata = MetadataExtractor(buffer)
        if (!metadata.hasMetadata()) {
            val dialogBuilder = AlertDialog.Builder(requireActivity())
            // set message of alert dialog
            dialogBuilder.setMessage("No metadata was found in your imported model. Please make sure your model has metadata before importing it.")
                .setCancelable(true)
            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("No metadata found.")
            // show alert dialog
            alert.show()
            return false
        }
        else {
            return true
        }
    }

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
    }

}