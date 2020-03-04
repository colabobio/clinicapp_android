package org.broadinstitute.clinicapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.broadinstitute.clinicapp.data.source.local.entities.MasterVariables
import io.reactivex.Flowable
import io.reactivex.Single


@Dao
interface MasterVariablesDao {

    /**
     * Insert a list in the database. If the item already exists, replace it.
     *
     * @param masterVariablesList to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAll(masterVariablesList: List<MasterVariables>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    fun insertAllSingle(masterVariablesList: List<MasterVariables>): Single<List<Long>>

    @Query("SELECT * FROM MasterVariables WHERE variable_category = :category AND is_active = 1")
    fun getMasterVariables(category: String): List<MasterVariables>

    @Query("SELECT DISTINCT variable_category FROM MasterVariables WHERE is_active = 1 ORDER BY variable_category ASC")
    fun getAllCategories(): Flowable<List<String>>

    @Query("SELECT count(*) FROM MasterVariables")
    fun getCount(): Single<Int>

    @Query("SELECT * FROM MasterVariables WHERE master_variables_id = :ids")
    fun getVariableByID(ids: Long): Single<MasterVariables>

    @Query("SELECT MAX(last_modified) FROM MasterVariables")
    fun getLastModified(): Long

    @Query("SELECT master_variables_id FROM MasterVariables WHERE is_searchable = 1")
    fun getSearchVariables(): Single<List<Long>>

}