package com.em_projects.bouncer.views

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.em_projects.bouncer.*
import com.em_projects.infra.activity.BasicActivity
import kotlinx.android.synthetic.main.activity_onboarding_permissions.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class OnBoardPermissionsActivity : BasicActivity(), EasyPermissions.PermissionCallbacks {
    private val TAG: String = "OnBoardPermissionsAct"

    // Permission Components
    companion object {
        const val RC_APP_PERMISSION: Int = 123
    }

    private val appPermissions: Array<String> = arrayOf(
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_SYNC_SETTINGS,
            Manifest.permission.WRITE_SYNC_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.MODIFY_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CALL_LOG
    )
    private var permissionGranted: Boolean = false

    // Remote config params
    private var showSkipPermissionsButton: Boolean = true

    // Helpers
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_permissions)
        Log.d(TAG, "onCreate")

        context = this@OnBoardPermissionsActivity

        allowPermissionsButton.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View?) {
                // TODO report click
                EasyPermissions.requestPermissions(
                        this@OnBoardPermissionsActivity,
                        context.getString(R.string.must_garnt_permission),
                        RC_APP_PERMISSION,
                        *appPermissions)
            }
        })

        // TODO Remote config
        var visibility = if (showSkipPermissionsButton) View.VISIBLE else View.INVISIBLE
        skipPermissions.visibility = visibility
        skipPermissions.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View?) {
                // TODO report click
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getPermissions()
    }

    @AfterPermissionGranted(RC_APP_PERMISSION)
    private fun getPermissions() {
        if (hasAppPermissions()) {
            Log.d(TAG, "appPermissionsTask")
            moveToNextScreen()
        }
    }

    private fun moveToNextScreen() {

        // save the time splash is closed
        BouncerProperties.LAST_ON_PAUSE_TIME = System.currentTimeMillis()

        // if automatic lock is checked start bouncer login activity, else start main screen
        val intent: Intent
        val isRequestLock = ((BouncerApplication.getApplication().getUserSession<Any>() as BouncerUserSession).IsAutomaticLock
                || (BouncerApplication.getApplication().getUserSession<Any>() as BouncerUserSession).IsRequestPasswordTimer)
        intent = if (isRequestLock) Intent(baseContext, BouncerLoginActivity::class.java) else Intent(baseContext, BouncerActivity::class.java)
        startNewActivity(intent, true)
    }


// ************************************   EasyPermissions   ************************************

    private fun hasAppPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, *appPermissions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult")

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size)
        // Check if it not happen when permissions have been pre granted

        permissionGranted = true
        moveToNextScreen()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size)

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (!permissionGranted) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                AppSettingsDialog.Builder(this).build().show()
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult")

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            val yes = getString(R.string.yes)
            val no = getString(R.string.no)

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                    this,
                    getString(
                            R.string.returned_from_app_settings_to_activity,
                            if (hasAppPermissions()) yes else no
                    ),
                    Toast.LENGTH_LONG
            ).show()

            // Check if it not happen when permissions have been pre granted
        }
    }

}