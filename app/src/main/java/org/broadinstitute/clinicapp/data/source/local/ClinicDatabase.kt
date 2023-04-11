package org.broadinstitute.clinicapp.data.source.local


import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.data.source.local.dao.*
import org.broadinstitute.clinicapp.data.source.local.entities.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import javax.crypto.KeyGenerator


@Database(
    entities = [MasterVariables::class, MasterStudyForms::class, StudyFormVariables::class, MasterStudyData::class, StudyData::class, Patient::class, SyncStatus::class],
    version = 1,
    exportSchema = false //Not to keep schema version history backups.
)

abstract class ClinicDatabase : RoomDatabase() {
    abstract fun getMasterVariablesDao(): MasterVariablesDao

    abstract fun getMasterStudyFormsDao(): MasterStudyFormsDao

    abstract fun getStudyFormVariablesDao(): StudyFormVariablesDao

    abstract fun getMasterStudyDataDao(): MasterStudyDataDao

    abstract fun getStudyDataDao(): StudyDataDao

    abstract fun getPatientDao(): PatientDao

    abstract fun getSyncStatusDao(): SyncStatusDao

    companion object {
        private const val ALGORITHM_AES = "AES"
        private const val KEY_SIZE = 256
        private const val DB_NAME = "clinic_database.db"

        //Generates a new (if non-existing) passphrase and saves to the context's file directory
        fun passPhraseGen(context: Context): String {
            val filePass = File(context.filesDir, "secretPass.txt")
            if (!filePass.exists()) {
                filePass.createNewFile()
                val fosPass = FileOutputStream(filePass)
                val dataToWrite = generatePassphrase().toString()
                fosPass.write(dataToWrite.toByteArray())
                fosPass.close()
            }
            // Read data from file using a FileInputStream
            val inputStream = FileInputStream(File(context.filesDir, "secretPass.txt"))
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                stringBuilder.append(String(buffer, 0, bytesRead))
                bytesRead = inputStream.read(buffer)
            }
            inputStream.close()
            val dataRead = stringBuilder.toString()
            return dataRead
        }

        fun generatePassphrase(): ByteArray {
            val keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES)
            keyGenerator.init(KEY_SIZE)
            return keyGenerator.generateKey().encoded
        }


        @Volatile //The value of a volatile variable will never be cached, and all writes and reads will be done to and from the main memory. This helps make sure the value of INSTANCE is always up-to-date and the same to all execution threads.
        // It means that changes made by one thread to INSTANCE are visible to all other threads immediately, and you don't get a situation where, say, two threads each update the same entity in a cache, which would create a problem.
        private var INSTANCE: ClinicDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): ClinicDatabase {
            //ignores the old passphrase
//            val pass = context.getString(R.string.encrypt_details)
            //Generates a passphrase that is saved externally to file directory
            val pass = passPhraseGen(context)
            val passphrase = SQLiteDatabase.getBytes(pass.toCharArray())
            val factory = SupportFactory(passphrase)
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ClinicDatabase::class.java,
                        DB_NAME
                    )
                      //  .addMigrations(MIGRATION_1_2)
                        .openHelperFactory(factory)
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }

    }
}