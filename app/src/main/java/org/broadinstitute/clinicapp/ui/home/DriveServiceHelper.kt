package org.broadinstitute.clinicapp.ui.home

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Pair
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper() {
    private val mExecutor: Executor = Executors.newSingleThreadExecutor()

    fun createFilePickerIntent(): Intent? {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/octet-stream"
        return intent
    }

    fun openFileUsingStorageAccessFramework(
        contentResolver: ContentResolver, uri: Uri
    ): Task<Pair<String?, String?>?> {

        lateinit var modifiedTime: String
        lateinit var name: String

        return Tasks.call(mExecutor) {
            contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    uri.path?.let { Log.e("TAG", it) }
                    name = cursor.getString(2)
                    modifiedTime = cursor.getString(6)

                } else {
                    throw IOException("Empty cursor returned for file.")
                }
            }

            Pair.create(name, modifiedTime)

        }
    }
}