package hu.bme.hit.hu.mcold.gordian.exchange

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import hu.bme.gordian.hu.mcold.gordian.R
import hu.bme.gordian.hu.mcold.gordian.databinding.FragmentReadQrBinding
import hu.bme.hit.hu.mcold.gordian.MainActivity
import hu.bme.hit.hu.mcold.gordian.common.toData
import hu.bme.hit.hu.mcold.gordian.contacts.Contact
import hu.bme.hit.hu.mcold.gordian.viewModel.ChatViewModel

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
            activity.runOnUiThread {
                onReadSuccess(it)
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

    private fun Contact.toContactWithKey(result: Result): Contact {
        val bytes = result.text.toData()
        val (uid, keyProviders) = viewModel.security.processQrDataAsReader(bytes)
        Log.d("FECO", "Paired userID size: ${uid.size}")
        return Contact(
            id = id,
            owner = owner,
            uniqueId = uid,
            name = name,
            keys = keyProviders
        )
    }

    private fun onReadSuccess(result: Result){
        val newContact = viewModel.currentContact!!.toContactWithKey(result)
        if(!viewModel.startHello(newContact)){
            val activity = requireActivity() as MainActivity
            activity.showSnack("Pairing already in progress")
        }
        findNavController().navigate(R.id.from_read_to_confirm)
    }
}