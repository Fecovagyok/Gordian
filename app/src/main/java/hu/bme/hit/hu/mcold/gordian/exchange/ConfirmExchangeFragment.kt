package hu.bme.gordian.hu.mcold.gordian.exchange

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.szakchat.R
import com.example.szakchat.common.END
import com.example.szakchat.common.ERROR
import com.example.szakchat.common.MSG
import com.example.szakchat.databinding.ConfirmExchangeLayoutBinding
import com.example.szakchat.viewModel.ChatViewModel

class ConfirmExchangeFragment : Fragment() {

    private var _binding: ConfirmExchangeLayoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ConfirmExchangeLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Start pairing over server
        viewModel.pairData.observe(viewLifecycleOwner){
            when(it?.state){
                MSG -> {
                    binding.confirmSmallText.setText(it.msg)
                }
                ERROR -> {
                    binding.confirmBigText.setText(R.string.error)
                    binding.confirmSmallText.setText(it.msg)
                    binding.confirmErrorImg.visibility = View.VISIBLE
                    binding.exchangeConfirmProgress.visibility = View.GONE
                    setButtonOnError()
                }
                END -> {
                    binding.confirmBigText.setText(R.string.success)
                    binding.confirmSmallText.text = ""
                    binding.confirmDoneImg.visibility = View.VISIBLE
                    binding.exchangeConfirmProgress.visibility = View.GONE
                    setButtonOnSuccess()
                }
            }
        }

    }

    private fun setButtonOnError(){
        binding.confirmBackBtn.setText(R.string.try_again)
        binding.confirmBackBtn.isEnabled = true
        binding.confirmBackBtn.setOnClickListener {
            findNavController().popBackStack(R.id.exchangeKeyFragment, inclusive = false)
        }
    }

    private fun setButtonOnSuccess(){
        binding.confirmBackBtn.setText(R.string.get_back_messaging)
        binding.confirmBackBtn.setOnClickListener {
            findNavController().popBackStack(R.id.SecondFragment, inclusive = false)
        }
        binding.confirmBackBtn.isEnabled = true
    }

    /*private inline fun navigateAfter(crossinline block: () -> Unit) = lifecycleScope.launch(Dispatchers.IO) {
        delay(3000)
        withContext(Dispatchers.Main){
            block()
        }
    }*/

}