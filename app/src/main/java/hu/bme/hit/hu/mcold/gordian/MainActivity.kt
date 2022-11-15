package hu.bme.gordian.hu.mcold.gordian

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.szakchat.databinding.ActivityMainBinding
import com.example.szakchat.extensions.toUserID
import com.example.szakchat.permissions.MyPerm
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.NetworkManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var prefs: SharedPreferences // For settings
    private val perm = MyPerm(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.initNetwork(prefs.getString(NetworkManager.IP_KEY, null)?: NetworkManager.DEFAULT_IP)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = initNavGraph()
        initActionBar(navController)
        viewModel.networking.networkStatus.observe(this) {
            if(it.normal)
                binding.contentMain.statusBar.setBackgroundResource(R.color.statusNormalColor)
            else
                binding.contentMain.statusBar.setBackgroundResource(R.color.statusErrorColor)
            binding.contentMain.statusBar.text = it.message
        }
        perm.askPermission()
    }

    private fun initNavGraph(): NavController {
        val dest = initSelfId()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(dest)
        navController.graph = navGraph
        return navController
    }

    private fun initActionBar(navController: NavController) {
        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun restoreCredentials(): Boolean {
        if(viewModel.networking.self != null) return true
        val id = getPreferences(Context.MODE_PRIVATE).getString(NetworkManager.SELF_KEY, null) ?: return false
        val pass = ChatApplication.safePrefs.getString(NetworkManager.PASS_KEY, null)
        viewModel.networking.initSelfCredentials(id.toUserID(), pass!!)
        viewModel.networking.username = prefs.getString(NetworkManager.NAME_KEY, null)
        Log.d("FECO", "id was present, credentials loaded from prefs")
        return true
    }

    private fun initSelfId(): Int {
        return if(viewModel.networking.self != null){
            R.id.FirstFragment
        } else {
            if(!restoreCredentials())
                R.id.graph_login_fragment
            else {
                R.id.FirstFragment
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val controller = findNavController(R.id.nav_host_fragment_content_main)
                controller.navigate(R.id.preferences_fragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showSnack(@StringRes msg: Int){
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
    }

    fun showSnack(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun permGranted(){

    }

    fun permDenied(){

    }

    override fun onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        prefs?: return
        if(key == NetworkManager.IP_KEY){
            viewModel.networking.ip = prefs.getString(key, null)?: NetworkManager.DEFAULT_IP
        }
    }

    fun logout() {
        viewModel.networking.logout(getPreferences(Context.MODE_PRIVATE))
    }
}