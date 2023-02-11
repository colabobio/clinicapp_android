package org.broadinstitute.clinicapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.android.synthetic.main.pop_create_studyform.view.*
import org.broadinstitute.clinicapp.Config
import org.broadinstitute.clinicapp.Constants
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.api.ApiService
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.data.source.local.entities.StudyFormDetail
import org.broadinstitute.clinicapp.ui.OnSyncInteractionListener
import org.broadinstitute.clinicapp.ui.login.LoginActivity
import org.broadinstitute.clinicapp.ui.profile.ProfileActivity
import org.broadinstitute.clinicapp.ui.studyform.CreateFormActivity
import org.broadinstitute.clinicapp.ui.viewvariables.ViewVariablesActivity
import org.broadinstitute.clinicapp.util.CommonUtils


class HomeActivity : BaseActivity(), HomeContract.View,
    NavigationView.OnNavigationItemSelectedListener, OnSyncInteractionListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_ID = 1
    }

    private lateinit var listAdapter: StudyFormsAdapter
    private lateinit var searchView: SearchView
    private lateinit var presenter: HomePresenter
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var syncDialog: Dialog? = null

    override fun setUp() {
        if(pref.readBooleanFromPref(Constants.PrefKey.PREF_INITIAL_LAUNCH, true))
            presenter.checkMasterVariables(0)
        else presenter.checkStudyFormsInDB(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        presenter = HomePresenter(this, this, pref)

        if (Constants.LOCATION_ENABLED) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            if (checkAndRequestPermissions()) {
                getLocation()
            }
        }

        fab.setOnClickListener {
            showCreationFormDialog()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toolbar.setNavigationIcon(R.drawable.ic_hamburger_icon)
        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger_icon)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val linearLayoutManager = LinearLayoutManager(this)
        rvStudyForms.layoutManager = linearLayoutManager
        listAdapter = StudyFormsAdapter(userId, this)
        rvStudyForms.adapter = listAdapter
        setUp()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val fineLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                REQUEST_LOCATION_PERMISSION_ID
            )
            return false
        }
        return true
    }

    private fun isLocationEnabled(): Boolean {
        if (Constants.LOCATION_ENABLED) return false
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION_ID -> {

                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val perms = HashMap<String, Int>()
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {
                        getLocation()
                    }
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    ) {
                        explain()
                    }
                }
            }
        }

    }

    override fun onSyncClick(item: StudyFormDetail) {
        if(isNetworkConnected)presenter.syncIndividualForm(item)
        else showSnackBarMessage(getString(R.string.network_error))
    }


    private fun getLocation() {
        if (isLocationEnabled() &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mFusedLocationClient.lastLocation?.addOnCompleteListener(this) { task ->
                val location: Location? = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    saveLocation(location.latitude, location.longitude)
                }
            }
        } else {
            Snackbar.make(drawer_layout, "Turn on location", Snackbar.LENGTH_SHORT)
                .setAction(R.string.allow) {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .show()
        }
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        pref.writeFloatToPref(Constants.PrefKey.PREF_LATITUDE, latitude.toFloat())
        pref.writeFloatToPref(Constants.PrefKey.PREF_LONGITUDE, longitude.toFloat())
    }

    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLastLocation?.let { saveLocation(it.latitude, mLastLocation.longitude) }
        }
    }


    private fun explain() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(getString(R.string.permission_location_access))
            .setPositiveButton(R.string.yes) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse(packageName)
                    )
                )
            }
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
        dialog.show()
    }


    @SuppressLint("InflateParams")
    private fun showCreationFormDialog() {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.pop_create_studyform, null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)

        //show dialog
        val mAlertDialog = mBuilder.show()
        //login button click of custom layout
        mDialogView.pop_create_scratch.setOnClickListener {
            mAlertDialog.dismiss()
            intent = Intent(this, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.CREATE_FROM_SCRATCH_STUDY_FORM
                )

            startActivity(intent)
        }
        //cancel button click of custom layout
        mDialogView.pop_create_existing.setOnClickListener {
            intent = Intent(this, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.CREATE_FROM_TEMPLATE_STUDY_FORM
                )

            startActivity(intent)
            mAlertDialog.dismiss()
        }

        if (isNetworkConnected) {
            mDialogView.pop_import_online.isEnabled = true
            mDialogView.pop_import_online.setBackgroundResource(R.drawable.button_bg)
        } else {
            mDialogView.pop_import_online.setBackgroundResource(R.drawable.inactive_button_bg)
            mDialogView.pop_import_online.isEnabled = false
        }
        mDialogView.pop_import_online.setOnClickListener {
            intent = Intent(this, CreateFormActivity::class.java)
                .putExtra(
                    Constants.BundleKey.CREATE_STUDY_FORM_KEY,
                    Constants.CallingPageValue.IMPORT_FROM_ONLINE_STUDY_FORM
                )

            startActivity(intent)
            mAlertDialog.dismiss()
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search_form, menu)
        val searchItem = menu!!.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.queryHint = getString(R.string.search_study_forms)

        val searchSubmit =
            searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as ImageView
        searchSubmit.setImageResource(R.mipmap.ic_search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) {
                    presenter.getStudyFormsFromDB("")
                }
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
               //From DB
                presenter.getStudyFormsFromDB(query.trim())
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.CreateFormLayout -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
//            R.id.nav_logout -> {
//                if(isNetworkConnected)presenter.checkUnSyncData()
//                else onError(R.string.network_error)
//            }
            R.id.nav_view_mv -> {
                intent = Intent(this, ViewVariablesActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_profile -> {
                intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_sign_out -> {
                handleLogout(getString(R.string.logout_message), getString(R.string.yes), getString(R.string.no))
//                finish()
            }

//            R.id.nav_manual_sync -> {
//                if (isNetworkConnected) {
//                    showSyncDialog()
//                    presenter.getSyncTimes()
//
//                } else {
//                    onError(getString(R.string.network_error))
//                }
//            }



        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun showStudyForms(list: PagedList<StudyFormDetail>) {
        listAdapter.submitList(list)
        if(listAdapter.itemCount == 0){
            showEmptyWarning(true)
        }else{
            showEmptyWarning(false)
        }
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun showSyncDialog() {
        syncDialog = showSyncLoadingDialog(this)
        sync()

    }

    @SuppressLint("CheckResult")
    private fun sync() {
        presenter.source
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { t ->
                updateProgress(t.show, t.msg)
            }

    }

    @SuppressLint("SetTextI18n")
    override fun updateProgress(show: Boolean, syncMessage: String) {
        if (syncDialog != null && syncDialog?.isShowing!!) {
            val progressBar = syncDialog?.findViewById<ProgressBar>(R.id.pb_loading)
            val initialProgress = progressBar?.progress
            // Remove study forms sync from Global sync so we increase progress limit 20 to 25, now its call in 4 steps
            progressBar?.progress = initialProgress?.plus(25)!!

            if (initialProgress > 80) {
                syncDialog?.dismiss()
                showSnackBarMessage(getString(R.string.sync_success))
            }
            val txt = syncDialog?.findViewById<TextView>(R.id.pb_progressTxt)
            txt?.text = "$initialProgress/100"

            if (!show) {
                syncDialog?.dismiss()
                if(syncMessage == getString(R.string.unAuthorized_error)) {
                    handleAuthError()
                }else{
                    showSnackBarMessage(syncMessage)
                }
            }

        }

    }

    override fun showSnackBarMessage(message: String) {
       onError(message)
    }

    override fun showToastMessage(message: String) {
        showMessage(message)
    }

    override fun showEmptyWarning(isEmpty: Boolean) {
        if(isEmpty) form_emptyView.visibility = View.VISIBLE
        else form_emptyView.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.unsubscribe()
    }

    override fun handleLogout(message : String, positiveOption : String, negativeOption : String) {

        CommonUtils.showDialog(this,
            getString(R.string.warning),
             message,
            positiveOption,
            negativeOption,
            object : CommonUtils.DialogCallback {
                override fun positiveClick() {
                   this@HomeActivity.startActivity(
                     Intent(
                       this@HomeActivity,
                       LoginActivity::class.java
                       )
                   )
                   finish()
                }

                override fun negativeClick() {
                    showProgress(false)
                }
            })

    }

    override fun onStop() {
        super.onStop()
        if (Constants.LOCATION_ENABLED) {
            mFusedLocationClient.removeLocationUpdates(
                mLocationCallback
            )
        }
    }

    private fun showSyncLoadingDialog(context: Context): Dialog {
        val progressDialog = Dialog(context)

        progressDialog.setContentView(R.layout.sync_dialog)
        val progressBar = progressDialog.findViewById<ProgressBar>(R.id.pb_loading)
        val title = progressDialog.findViewById<AppCompatTextView>(R.id.pb_loadingTitle)
        progressBar.max = 100 // Progress Dialog Max Value
        progressBar.isIndeterminate = false
        title.text = getString(R.string.manual_sync)

        progressDialog.setCancelable(true)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        return progressDialog

    }


}
