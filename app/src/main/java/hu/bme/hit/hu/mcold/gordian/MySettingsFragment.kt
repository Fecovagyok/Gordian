package hu.bme.gordian.hu.mcold.gordian

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.szakchat.common.reSetupActionBar

class MySettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val serverPref: EditTextPreference? = findPreference("SERVER_ADDRESS")
        serverPref?.setOnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_NUMBER
        }
        val logoutPref: Preference? = findPreference("LOGOUT")
        logoutPref?.setOnPreferenceClickListener {
            val a = requireActivity() as MainActivity
            a.logout()
            reSetupActionBar(a, R.id.graph_login_fragment){
                it.navigate(R.id.from_preference_to_login)
            }
            true
        }
    }
}