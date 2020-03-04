package org.broadinstitute.clinicapp.ui.viewvariables

import android.os.Bundle
import org.broadinstitute.clinicapp.R
import org.broadinstitute.clinicapp.base.BaseActivity
import org.broadinstitute.clinicapp.ui.studyform.variableselection.VCFragment

class ViewVariablesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_view_variables)
        setUp()
    }

    override fun setUp() {
        val transaction = this.supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.flViewVars,
            VCFragment.newInstance(true)
        )
        transaction.commit()
    }
}
