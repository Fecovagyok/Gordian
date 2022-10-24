package com.example.szakchat

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.preference.PreferenceManager
import com.example.szakchat.databinding.ActivityMainBinding
import com.example.szakchat.permissions.MyPerm
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.NetworkManager
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var prefs: SharedPreferences
    private val perm = MyPerm(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.initNetwork(prefs.getString(NetworkManager.IP_KEY, null)?: NetworkManager.DEFAULT_IP)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        initSelfId(navController)
        viewModel.networking.networkStatus.observe(this) {
            if(it.normal)
                binding.contentMain.statusBar.setBackgroundResource(R.color.statusNormalColor)
            else
                binding.contentMain.statusBar.setBackgroundResource(R.color.statusErrorColor)
            binding.contentMain.statusBar.text = it.message
        }
        if(android.os.Build.VERSION.SDK_INT >= 23)
            perm.askPermission()
    }

    private fun initSelfId(navController: NavController) {
        if (viewModel.networking.self == null) {
            val self = getPreferences(MODE_PRIVATE).getString(NetworkManager.SELF_KEY, null)
            if (self == null) {
                navController.navigate(R.id.action_First_to_self)
            } else {
                viewModel.networking.self = self
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
                if(controller.currentDestination?.id != R.id.giveSelfFragment)
                    controller.navigate(R.id.giveSelfFragment)
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
}