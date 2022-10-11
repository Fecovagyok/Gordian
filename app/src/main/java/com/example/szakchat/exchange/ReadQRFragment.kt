package com.example.szakchat.exchange

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.szakchat.MainActivity
import com.example.szakchat.databinding.FragmentReadQrBinding
import com.example.szakchat.viewModel.ChatViewModel
import com.google.common.util.concurrent.Futures.nonCancellationPropagating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReadQRFragment : Fragment() {
    private var _binding: FragmentReadQrBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReadQrBinding.inflate(layoutInflater, container, false)
        val activity = requireActivity() as MainActivity
        binding.qrPreview.implementationMode = PreviewView.ImplementationMode.PERFORMANCE

        lifecycleScope.launch(Dispatchers.IO) {
            val cameraProvider = nonCancellationPropagating(activity.cameraProviderFuture).await()
            bindPreview(cameraProvider)
        }

        return binding.root
    }

    private suspend fun bindPreview(provider: ProcessCameraProvider){
        val preview = Preview.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .build()
        Log.d("FECO", "Res: ${preview.resolutionInfo?.resolution}")
        val camSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        withContext(Dispatchers.Main) {
            preview.setSurfaceProvider(binding.qrPreview.surfaceProvider)
            Log.d("FECO", "Res:  ${preview.resolutionInfo}")
            val camera = provider.bindToLifecycle(viewLifecycleOwner, camSelector, preview)
        }
    }
}