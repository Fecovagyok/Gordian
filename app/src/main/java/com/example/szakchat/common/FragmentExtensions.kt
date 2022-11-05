package com.example.szakchat.common

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.szakchat.MainActivity

inline fun Fragment.reSetupActionBar(a: MainActivity, startDest: Int, navigateBlock: (NavController) -> Unit){
    val controller = findNavController()
    controller.graph.setStartDestination(startDest)
    val configuration = AppBarConfiguration(controller.graph)
    navigateBlock(controller)
    a.setupActionBarWithNavController(controller, configuration)
}
