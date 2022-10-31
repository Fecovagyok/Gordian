package com.example.szakchat.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.example.szakchat.MainActivity
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentReadQrBinding
import com.example.szakchat.viewModel.ChatViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result

class ReadQRFragment : Fragment() {
    private var _binding: FragmentReadQrBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()
    private var _codeScanner: CodeScanner? = null
    private val codeScanner get() = _codeScanner!!
    private var _activity: MainActivity? = null
    private val activity: MainActivity get() = _activity!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReadQrBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _activity = requireActivity() as MainActivity
        _codeScanner = CodeScanner(activity, binding.qrPreview)
        initScanner()
    }

    private fun initScanner(){
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE) // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            onReadSuccess(it)
            activity.runOnUiThread {
                activity.showSnack("Success")
                findNavController().popBackStack(R.id.SecondFragment, inclusive = false)
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            activity.runOnUiThread {
                activity.showSnack(it.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun onReadSuccess(result: Result){
        viewModel.security.setSecureBytes(result.text)
    }
}