/*
 * Copyright 2024 Takumi Oe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tkm0e.telephonychecker

import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewUtil.setWindowInsetPadding(findViewById(R.id.scroll_wrapper))
        ViewUtil.setWindowInsetPadding(findViewById(R.id.fab_wrapper))

        container = findViewById(R.id.container)
        findViewById<SwipeRefreshLayout>(R.id.refresh).apply {
            setProgressBackgroundColorSchemeColor(getColor(R.color.refresh_progress_background))
            setColorSchemeColors(getColor(R.color.refresh_progress_indicator))
            setProgressViewOffset(
                false,
                progressViewStartOffset,
                progressViewEndOffset
            )
            setOnRefreshListener {
                if (checkPhonePermission()) {
                    reload()
                } else {
                    requestPhonePermission()
                }
                isRefreshing = false
            }
        }
        findViewById<Button>(R.id.btn_network).setOnClickListener {
            if (!openNetworkSettings()) {
                showMessage(R.string.unsupported_device, R.drawable.ic_fail)
            }
        }
        findViewById<Button>(R.id.btn_sim).setOnClickListener {
            if (!openSimSettings()) {
                showMessage(R.string.unsupported_device, R.drawable.ic_fail)
            }
        }
        findViewById<Button>(R.id.fab).setOnClickListener {
            takeScreenshot()
        }

        if (!checkPhonePermission()) {
            requestPhonePermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPhonePermission()) {
            reload()
        }
    }

    private fun takeScreenshot() {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }
        val success = Screenshot.take(container)
        val message = if (success) R.string.screenshot_saved else R.string.screenshot_failed
        val icon = if (success) R.drawable.ic_done else R.drawable.ic_fail
        showMessage(message, icon)
    }

    private fun openNetworkSettings(): Boolean {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun openSimSettings(): Boolean {
        val intent = Intent().apply {
            setClassName("com.android.settings", "com.android.settings.Settings\$SimSettingsActivity")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return openSimProfilesSettings()
            }
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun openSimProfilesSettings(): Boolean {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun reload() {
        container.removeAllViews()
        addBuildSection()
        addBuildVersionSection()
        addTelephonyManagerSection()
        addSubscriptionManagerSection()
        addFooter()
    }

    private fun addBuildSection() {
        addSection(SECTION_TYPE_MAIN, "Build")
        addItem(ITEM_TYPE_DEFAULT, "PRODUCT", Build.PRODUCT)
        addItem(ITEM_TYPE_DEFAULT, "DEVICE", Build.DEVICE)
        addItem(ITEM_TYPE_DEFAULT, "MODEL", Build.MODEL)
        addItem(ITEM_TYPE_DEFAULT, "MANUFACTURER", Build.MANUFACTURER)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            addItem(ITEM_TYPE_DEFAULT, "SOC_MODEL", Build.SOC_MODEL)
            addItem(ITEM_TYPE_DEFAULT, "SOC_MANUFACTURER", Build.SOC_MANUFACTURER)
        }
        addItem(ITEM_TYPE_DEFAULT, "SUPPORTED_ABIS", "${Build.SUPPORTED_ABIS?.joinToString(", ")}")
        addItem(ITEM_TYPE_DEFAULT, "DISPLAY", Build.DISPLAY)
    }

    private fun addBuildVersionSection() {
        addSection(SECTION_TYPE_SUB, "VERSION")
        addItem(ITEM_TYPE_DEFAULT, "INCREMENTAL", Build.VERSION.INCREMENTAL)
        addItem(ITEM_TYPE_DEFAULT, "RELEASE", Build.VERSION.RELEASE)
        addItem(ITEM_TYPE_DEFAULT, "SDK_INT", "${Build.VERSION.SDK_INT}")
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    private fun addTelephonyManagerSection() {
        addSection(SECTION_TYPE_MAIN, "TelephonyManager")
        val telephonyManager = getSystemService(TelephonyManager::class.java)
        addItem(ITEM_TYPE_DEFAULT, "Line1Number", telephonyManager.line1Number ?: "")
        addItem(ITEM_TYPE_DEFAULT, "SimState", TelephonyHelper.getSimStateName(telephonyManager.simState))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addItem(ITEM_TYPE_DEFAULT, "SimCarrierIdName", "${telephonyManager.simCarrierIdName}")
            addItem(ITEM_TYPE_DEFAULT, "SimCarrierId", "${telephonyManager.simCarrierId}")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addItem(ITEM_TYPE_DEFAULT, "DataEnabled", "${telephonyManager.isDataEnabled}")
        }
        addItem(ITEM_TYPE_DEFAULT, "DataState", TelephonyHelper.getDataStateName(telephonyManager.dataState))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addItem(ITEM_TYPE_DEFAULT, "DataNetworkType", TelephonyHelper.getDataNetworkTypeName(telephonyManager.dataNetworkType))
        } else {
            addItem(ITEM_TYPE_DEFAULT, "NetworkType", TelephonyHelper.getDataNetworkTypeName(telephonyManager.networkType))
        }
    }

    @SuppressLint("MissingPermission")
    private fun addSubscriptionManagerSection() {
        addSection(SECTION_TYPE_MAIN, "SubscriptionManager/Info")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addItem(ITEM_TYPE_DEFAULT, "DefaultSubscriptionId", "${SubscriptionManager.getDefaultSubscriptionId()}")
            addItem(ITEM_TYPE_DEFAULT, "DefaultVoiceSubscriptionId", "${SubscriptionManager.getDefaultVoiceSubscriptionId()}")
            addItem(ITEM_TYPE_DEFAULT, "DefaultSmsSubscriptionId", "${SubscriptionManager.getDefaultSmsSubscriptionId()}")
            addItem(ITEM_TYPE_DEFAULT, "DefaultDataSubscriptionId", "${SubscriptionManager.getDefaultDataSubscriptionId()}")
        }
        val subscriptionManager = getSystemService(SubscriptionManager::class.java)
        val infoList = subscriptionManager.activeSubscriptionInfoList
        if (infoList.isNullOrEmpty()) {
            addItem(ITEM_TYPE_DEFAULT,"(No SIM)", "")
            return
        }
        for (info in infoList) {
            addItem(ITEM_TYPE_PARENT,"SimSlotIndex", "${info.simSlotIndex}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addItem(ITEM_TYPE_CHILD, "PhoneNumber", subscriptionManager.getPhoneNumber(info.subscriptionId))
            }
            addItem(ITEM_TYPE_CHILD, "Number", info.number)
            addItem(ITEM_TYPE_CHILD, "DisplayName", "${info.displayName}")
            addItem(ITEM_TYPE_CHILD, "SubscriptionId", "${info.subscriptionId}")
            addItem(ITEM_TYPE_CHILD, "CarrierName", "${info.carrierName}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                addItem(ITEM_TYPE_CHILD, "CarrierId", "${info.carrierId}")
            }
            addItem(ITEM_TYPE_CHILD, "CountryIso", info.countryIso)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                addItem(ITEM_TYPE_CHILD, "Mcc/Mnc", "${info.mccString}/${info.mncString}")
            } else {
                addItem(ITEM_TYPE_CHILD, "Mcc/Mnc", "${info.mcc}/${info.mnc}")
            }
        }
    }

    private fun addSection(@SectionType type: Int, title: String) {
        val view = when (type) {
            SECTION_TYPE_SUB ->
                LayoutInflater.from(this).inflate(R.layout.list_section_sub, container, false)
            else ->
                LayoutInflater.from(this).inflate(R.layout.list_section_main, container, false)
        }.also {
            container.addView(it)
        }
        view.findViewById<TextView>(R.id.title).text = title
    }

    private fun addItem(@ItemType type: Int, title: String, value: String) {
        val view = when (type) {
            ITEM_TYPE_PARENT ->
                LayoutInflater.from(this).inflate(R.layout.list_item_parent, container, false)
            ITEM_TYPE_CHILD ->
                LayoutInflater.from(this).inflate(R.layout.list_item_child, container, false)
            else ->
                LayoutInflater.from(this).inflate(R.layout.list_item, container, false)
        }.also {
            container.addView(it)
        }
        view.findViewById<TextView>(R.id.title).text = title
        view.findViewById<TextView>(R.id.value).apply {
            text = value
            setOnClickListener {
                getSystemService(ClipboardManager::class.java)
                    .setPrimaryClip(ClipData.newPlainText(title, value))
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    showMessage(R.string.value_copied, R.drawable.ic_copy)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addFooter() {
        val view = LayoutInflater.from(this).inflate(R.layout.list_footer, container, false).also {
            container.addView(it)
        }
        view.findViewById<TextView>(R.id.ver).apply {
            text = "${getString(R.string.app_name)} v${BuildConfig.VERSION_NAME}"
            setOnClickListener {
                val intent = Intent(this@MainActivity, LicenseActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun showMessage(@StringRes message: Int, @DrawableRes icon: Int) {
        Snackbar.make(findViewById(R.id.main), message, Snackbar.LENGTH_SHORT).also {
            ViewUtil.setWindowInsetMargin(it.view)
            (it.view.layoutParams as CoordinatorLayout.LayoutParams).apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            }
            it.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                .setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    AppCompatResources.getDrawable(this, icon),
                    null,
                    null
                )
        }.show()
    }

//region Permissions
    private val requestPhonePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (granted[READ_PHONE_STATE] == true && granted[READ_PHONE_NUMBERS] == true) {
                return@registerForActivityResult
            }
        } else {
            if (granted[READ_PHONE_STATE] == true) {
                return@registerForActivityResult
            }
        }
        Toast.makeText(this, R.string.please_allow_phone_permission, Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun requestPhonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestPhonePermissionLauncher.launch(arrayOf(READ_PHONE_STATE, READ_PHONE_NUMBERS))
        } else {
            requestPhonePermissionLauncher.launch(arrayOf(READ_PHONE_STATE))
        }
    }

    private fun checkPhonePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            takeScreenshot()
            return@registerForActivityResult
        }
        Toast.makeText(this, R.string.please_allow_storage_permission, Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        startActivity(intent)
    }

    private fun requestStoragePermission() {
        requestStoragePermissionLauncher.launch(WRITE_EXTERNAL_STORAGE)
    }

    private fun checkStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }
//endregion

    companion object {
        @Retention(AnnotationRetention.SOURCE)
        @IntDef(ITEM_TYPE_DEFAULT, ITEM_TYPE_PARENT, ITEM_TYPE_CHILD)
        annotation class ItemType
        const val ITEM_TYPE_DEFAULT = 0
        const val ITEM_TYPE_PARENT = 1
        const val ITEM_TYPE_CHILD = 2

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(SECTION_TYPE_MAIN, SECTION_TYPE_SUB)
        annotation class SectionType
        const val SECTION_TYPE_MAIN = 0
        const val SECTION_TYPE_SUB = 1
    }
}