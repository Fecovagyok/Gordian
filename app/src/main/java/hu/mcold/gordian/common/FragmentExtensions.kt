package hu.mcold.gordian.common

import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.textfield.TextInputLayout
import hu.mcold.gordian.MainActivity

inline fun Fragment.reSetupActionBar(a: MainActivity, startDest: Int, navigateBlock: (NavController) -> Unit){
    val controller = findNavController()
    controller.graph.setStartDestination(startDest)
    val configuration = AppBarConfiguration(controller.graph)
    navigateBlock(controller)
    a.setupActionBarWithNavController(controller, configuration)
}

fun TextInputLayout.onError(str: String){
    error = str
    requestFocus()
}
