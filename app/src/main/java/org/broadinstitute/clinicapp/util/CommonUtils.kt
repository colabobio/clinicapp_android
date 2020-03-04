package org.broadinstitute.clinicapp.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.TableRow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.TextViewCompat
import kotlinx.android.synthetic.main.pop_variable_info.view.*
import org.broadinstitute.clinicapp.ClinicApp
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*


object CommonUtils {

    fun showLoadingDialog(context: Context): Dialog {
        val progressDialog = Dialog(context)
        progressDialog.show()
        if (progressDialog.window != null) {
            progressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        progressDialog.setContentView(R.layout.progress_dialog)
        //   progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        return progressDialog
        //  return ProgressDialog.show(context, "", "loading", true, false)
    }

    fun generateAdminId(userID: Long): String {
        return "P_" + userID + "_" + getPatientID()
    }

    fun generateTempId(type: String, userID: String): String {
        return "TMP_" + type + "_" + userID + "_" + System.currentTimeMillis()
    }

    fun generateTempId(type: String, userID: String, index: Int): String {
        return "TMP_" + type + "_" + userID + "_" + System.currentTimeMillis() + "_" + index
    }

    fun getTimezone(): String {
        val date = SimpleDateFormat("z", Locale.getDefault())
        return date.format(System.currentTimeMillis())
    }

    @SuppressLint("InflateParams")
    fun onInfoSelected(masterVariables: MasterVariables, context: Context) {
        val mDialogView =
            LayoutInflater.from(context).inflate(R.layout.pop_variable_info, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)

        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            mDialogView.txtVariableLabel.text =
                Html.fromHtml(masterVariables.label, Html.FROM_HTML_MODE_LEGACY)
        } else {
            mDialogView.txtVariableLabel.text = masterVariables.label
        }

        mAlertDialog.setTitle(masterVariables.label)

        mDialogView.txtVariableDescription.text =
            String.format(
                context.getString(R.string.variable_description),
                "\n      " + masterVariables.description
            )

        mDialogView.txtVariableType.text =
            String.format(context.getString(R.string.variable_type), masterVariables.type)

        mDialogView.txtVariableWhenAsked.text =  String.format(
            context.getString(R.string.variable_when_asked),whenAskedType(masterVariables.whenAsked, context))

        mDialogView.txtVariableIsMandatory.text = String.format(
            context.getString(R.string.variable_is_mandatory), masterVariables.isMandatory
        )
        mDialogView.txtVariableIsSearchable.text = String.format(
            context.getString(R.string.variable_is_searchable), masterVariables.isSearchable
        )

        if (masterVariables.type.toLowerCase(Locale.ENGLISH) == "cat") {

            addCategoryOptions(
                masterVariables.categoricalOpt1,
                createRow(context, mDialogView),
                context
            )
            addCategoryOptions(
                masterVariables.categoricalOpt2,
                createRow(context, mDialogView),
                context
            )
            addCategoryOptions(
                masterVariables.categoricalOpt3,
                createRow(context, mDialogView),
                context
            )
            addCategoryOptions(
                masterVariables.categoricalOpt4,
                createRow(context, mDialogView),
                context
            )
            addCategoryOptions(
                masterVariables.categoricalOpt5,
                createRow(context, mDialogView),
                context
            )
            mDialogView.tblCategoryOptions.visibility = View.VISIBLE
        } else {
            mDialogView.tblCategoryOptions.visibility = View.GONE
        }
    }

    private fun createRow(context: Context, mDialogView: View): TableRow {
        val row = TableRow(context)
        val lp = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT)
        row.layoutParams = lp

        mDialogView.tblCategoryOptions.addView(row)
        return row
    }

    private fun addCategoryOptions(
        categoryOption: String?,
        row: TableRow, context: Context
    ) {
        if (!TextUtils.isEmpty(categoryOption)) {
            val txtOption = AppCompatTextView(context)
            val lp = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 5, 20, 5)
            txtOption.text = categoryOption
            txtOption.layoutParams = lp
            TextViewCompat.setTextAppearance(txtOption, R.style.txtVariableInfo)
            row.addView(txtOption)
        }
    }

    fun convertDate(dateInMilliseconds: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return formatter.format(dateInMilliseconds)
    }

    private fun getPatientID(): String {
        val formatter = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault())
        return formatter.format(System.currentTimeMillis())
    }

    interface DialogCallback {

        fun positiveClick()
        fun negativeClick()
    }

    fun showDialog(
        context: Context,
        title: String,
        message: String, positiveBtn: String, negativeBtn: String,
        dialogCallback: DialogCallback
    ) {


        val builder = AlertDialog.Builder(context)
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(message)
        //performing positive action
        builder.setPositiveButton(positiveBtn) { _, _ ->
            dialogCallback.positiveClick()
        }
        if (negativeBtn.isNotEmpty()) {
            builder.setNegativeButton(negativeBtn) { _, _ ->
                dialogCallback.negativeClick()
            }
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()

    }


    data class DiffTime(val diff: Long) {
        var seconds = diff / 1000
        //private var minutes = seconds / 60
        //private var hours = minutes / 60
      //  var days = hours / 24
    }

    fun timeDiff(millisecond: Long): DiffTime {
        return DiffTime(millisecond)
    }

    private fun whenAskedType(whenAsked: Int, context: Context): String{

        return when (whenAsked) {
            0 -> context.getString(R.string.when_asked_0)

            1 -> context.getString(R.string.when_asked_1)

            2 -> context.getString(R.string.when_asked_2)
            else -> "Blank"

        }

    }
    
    fun getErrorMessage(throwable: Throwable) : String {
        return when (throwable) {
            is HttpException ->{
                val v: HttpException = throwable
                when {
                    v.code() == 401 -> ClinicApp.applicationContext().getString(R.string.unAuthorized_error)
                    v.code()==504 -> ClinicApp.applicationContext().getString(R.string.timeout_error)
                    v.code()==500 -> ClinicApp.applicationContext().getString(R.string.unknown_error)
                    else -> throwable.message()
                }

            }
            is SocketTimeoutException -> ClinicApp.applicationContext().getString(R.string.timeout_error)
            is IOException -> ClinicApp.applicationContext().getString(R.string.network_io_error)
            is SQLException -> {
                throwable.printStackTrace()
                ""
            }

            else ->
                ClinicApp.applicationContext().getString(R.string.unknown_error)
        }
    }
}