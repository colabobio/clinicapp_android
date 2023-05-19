package org.broadinstitute.clinicapp.ui.studydata.survey

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_about_study_form.view.*
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseFragment
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.email
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.fileFullNames
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.fileNames
import org.broadinstitute.clinicapp.ui.home.FragmentMyModels.Companion.filePaths
import java.util.EnumSet.range


class AboutFragment : BaseFragment(){


    private val model: SharedViewModel by activityViewModels()
    private var studyFormDetail: StudyFormDetail? = null
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studyFormDetail = model.selected.value

    }

    @SuppressLint("StringFormatInvalid", "CheckResult")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_about_study_form, container, false)

        view.txtHeader.text = getString(R.string.about_study_data)
        view.btn_confirm.visibility = View.VISIBLE
        progressBar = view.aboutProgressBar
        view.btn_confirm.text= getString(R.string.continue_str)
        view.btn_use_model.visibility = View.VISIBLE
        view.btn_use_model.text = "Apply Model"
        if(studyFormDetail!=null){
            view.about_study_form_details_title.text = studyFormDetail?.masterStudyForms?.title
            view.study_form_details_desc.text = studyFormDetail?.masterStudyForms?.description
        }

        view.btn_confirm.setOnClickListener {
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.add(R.id.flAddMoreVars, FillStudiesFragment.newInstance(),"Fill_data")
            transaction?.addToBackStack("about")
            transaction?.commit()
        }

        view.btn_use_model.setOnClickListener {

            var selectedFilePath = filePaths[0]
            var selectedFileIndex = 0

            var userFilesNames = emptyArray<String>()
            var userFilePaths = emptyArray<String>()

            var size = fileNames.size
            Log.e("TAG", fileFullNames[0])

            for (i in 0 until size) {
                if (email in fileFullNames[i]) {
                    userFilesNames = userFilesNames.plus(fileNames[i])
                    userFilePaths = userFilePaths.plus(filePaths[i])
                }
            }

            try {
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Choose a model")
                    .setSingleChoiceItems(userFilesNames, selectedFileIndex) {dialog, which ->
                        selectedFileIndex = which
                        selectedFilePath = userFilePaths[which]
                        Log.e("TAG", selectedFilePath)
                    }
                    .setPositiveButton("OK") { dialog, which ->
                        // transition to fragment that displays model output
                        // take all inputs to new fragment
                    }

                    .setNeutralButton("CANCEL") {dialog, which ->

                    }.show()
            }

            catch(ex: Exception){
                val dialogBuilder = AlertDialog.Builder(requireActivity())

                // set message of alert dialog
                dialogBuilder.setMessage("You have not imported any models from your Drive. Please import a model and then try again.")
                    .setCancelable(true)

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("No models imported.")
                // show alert dialog
                alert.show()
            }

        }

        model.source
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{
            if (it == true) {
                view.btn_confirm.isEnabled = false
                progressBar.visibility = View.VISIBLE
            } else {
                view.btn_confirm.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
        return view
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
            AboutFragment().apply {

            }

    }

}
