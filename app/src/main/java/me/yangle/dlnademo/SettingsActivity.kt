package me.yangle.dlnademo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import java.util.logging.Level
import java.util.logging.Logger

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.settings, SettingsFragment())
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private val logger = Logger.getLogger("org.fourthline.cling")

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val debugPreference: SwitchPreference? = findPreference("debug")
            debugPreference?.setDefaultValue(logger.level == Level.FINEST)
            debugPreference?.setOnPreferenceChangeListener { preference, newValue ->
                logger.level = if (newValue as Boolean) Level.FINEST else Level.INFO
                true
            }
        }
    }
}