package me.blog.korn123.easyphotomap.activities


import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.view.MenuItem
import me.blog.korn123.easyphotomap.R
import me.blog.korn123.easyphotomap.constants.Constant
import me.blog.korn123.easyphotomap.utils.CommonUtils
import org.apache.commons.lang.StringUtils

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsActivity : AppPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        mContext = baseContext

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.setting_activity_title)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return true
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean = isXLargeTablet(this)

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean = PreferenceFragment::class.java.name == fragmentName || GeneralPreferenceFragment::class.java.name == fragmentName

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {

        private var listener: SharedPreferences.OnSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (StringUtils.equals(key, "date_filter_setting")) {
                val switchPreference = findPreference("date_filter_setting") as SwitchPreference
                val checked = switchPreference.isChecked
                CommonUtils.saveBooleanPreference(activity, "date_filter_setting", checked)
            } else if (StringUtils.equals(key, "photo_size_setting")) {
                val listPreference = findPreference("photo_size_setting") as ListPreference
                val photoSizeKey = listPreference.value
                CommonUtils.saveStringPreference(mContext!!, "photo_size_setting", photoSizeKey)
            }
        }


        private fun initPreference() {
            val mAppVersionPreference: Preference = findPreference("aaf_app_version")
            val mOpenSourceLicensesInfo: Preference = findPreference("open_source_licenses")
            var pInfo: PackageInfo? = null
            try {
                pInfo = activity.packageManager.getPackageInfo(activity.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            val version = pInfo?.versionName
            mAppVersionPreference.title = "Easy Photo Map Version"
            mAppVersionPreference.summary = "v " + version
            mAppVersionPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val uri = Uri.parse("market://details?id=me.blog.korn123.easyphotomap")
                val goToMarket = Intent(Intent.ACTION_VIEW, uri)
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                        /*Intent.FLAG_ACTIVITY_NEW_DOCUMENT or*/
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                try {
                    startActivity(goToMarket)
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=me.blog.korn123.easyphotomap")))
                }

                false
            }
            mOpenSourceLicensesInfo.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val intent = Intent(mContext, WebViewActivity::class.java)
                intent.putExtra(Constant.OPEN_URL_INFO, "https://github.com/hanjoongcho/aaf-easyphotomap/blob/master/LICENSE.md")
                startActivity(intent)
                false
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            initPreference()
            setHasOptionsMenu(true)
        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        }

        override fun onPause() {
            super.onPause()
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private var mContext: Context? = null

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
    }

}
