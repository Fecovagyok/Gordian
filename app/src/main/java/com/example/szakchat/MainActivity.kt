package com.example.szakchat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.szakchat.contacts.AddContactDialog
import com.example.szakchat.contacts.Contact
import com.example.szakchat.databinding.ActivityMainBinding
import com.example.szakchat.viewModel.ChatViewModel
import com.example.szakchat.viewModel.NetworkViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ChatViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        setSupportActionBar(binding.toolbar)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        initSelfId(navController)
    }

    private fun initSelfId(navController: NavController) {
        if (viewModel.networking.self == null) {
            val self = getPreferences(MODE_PRIVATE).getString(NetworkViewModel.SELF_KEY, null)
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
            R.id.action_add -> {
                Log.d("FECO", "self: ${viewModel.networking.self}")
                val dialog = AddContactDialog()
                dialog.show(supportFragmentManager, "Add a contact")
                true
            }
            R.id.action_settings -> {
                val controller = findNavController(R.id.nav_host_fragment_content_main)
                controller.navigate(R.id.action_First_to_self)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}