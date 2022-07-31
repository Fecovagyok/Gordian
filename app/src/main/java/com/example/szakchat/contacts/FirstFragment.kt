package com.example.szakchat.contacts

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.szakchat.MainActivity
import com.example.szakchat.R
import com.example.szakchat.databinding.FragmentFirstBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val activity = requireActivity() as MainActivity
        adapter = ContactAdapter()
        Log.d("FECO", adapter.toString())

        activity.viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            for(i in contacts)
                Log.d("FECO", i.toString())
            adapter.submitList(contacts)
            Log.d("FECO", "Inside: ${adapter.itemCount.toString()}")
        }

        Log.d("FECO", "Outside: " + adapter.itemCount.toString())

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.listContact.adapter = adapter
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        //binding.buttonFirst.setOnClickListener {
         //   findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        //}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}