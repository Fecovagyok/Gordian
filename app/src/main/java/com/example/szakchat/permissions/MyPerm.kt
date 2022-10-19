package com.example.szakchat.permissions

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.szakchat.MainActivity

class MyPerm(private val activity: MainActivity) {
    companion object {
        const val permission = Manifest.permission.CAMERA
    }

    val launcher = activity.registerForActivityResult(
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