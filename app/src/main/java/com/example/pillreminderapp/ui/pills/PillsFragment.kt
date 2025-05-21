package com.example.pillreminderapp.ui.pills

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pillreminderapp.databinding.FragmentPillsBinding

class PillsFragment : Fragment() {

    private var _binding: FragmentPillsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val pillsViewModel =
            ViewModelProvider(this).get(PillsViewModel::class.java)

        _binding = FragmentPillsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textPills
        pillsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}