package hu.mcold.gordian.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import hu.mcold.gordian.MainActivity

class MyPerm(private val activity: MainActivity) {
    companion object {
        const val permission = Manifest.permission.CAMERA
    }

    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted)
            activity.permGranted()
        else
            activity.permDenied()
    }

    fun askPermission() {
        if(!checkPerm()){
            launcher.launch(permission)
        }
    }

    fun checkPerm(): Boolean{
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, permission)
    }
}
