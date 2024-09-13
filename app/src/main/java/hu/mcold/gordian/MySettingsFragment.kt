package hu.mcold.gordian

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import hu.mcold.gordian.R
import hu.mcold.gordian.common.reSetupActionBar

class MySettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val serverPref: EditTextPreference? = findPreference("SERVER_ADDRESS")
        serverPref?.setOnBindEditTextListener {
        }
        val logoutPref: Preference? = findPreference("LOGOUT")
        logoutPref?.setOnPreferenceClickListener {
            val a = requireActivity() as hu.mcold.gordian.MainActivity
            a.logout()
            reSetupActionBar(a, R.id.graph_login_fragment){
                it.navigate(R.id.from_preference_to_login)
            }
            true
        }
    }
}