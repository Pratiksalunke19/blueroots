package com.blueroots.carbonregistry.ui.credits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blueroots.carbonregistry.databinding.FragmentCreditIssuanceBinding
import com.blueroots.carbonregistry.viewmodel.CreditViewModel
import com.blueroots.carbonregistry.ui.adapters.CreditAdapter

class CreditIssuanceFragment : Fragment() {
    private var _binding: FragmentCreditIssuanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CreditViewModel
    private lateinit var creditAdapter: CreditAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditIssuanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[CreditViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        // Load credits
        viewModel.loadUserCredits()
    }

    private fun setupRecyclerView() {
        creditAdapter = CreditAdapter()
        binding.recyclerViewCredits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = creditAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.credits.observe(viewLifecycleOwner) { credits ->
            creditAdapter.submitList(credits)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.textCreditsTitle.text = if (isLoading) "Loading..." else "Your Carbon Credits"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
