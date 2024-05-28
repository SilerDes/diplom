package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.employee

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentSessionConfigureEmployeeBinding
import com.kazbekov.invent.main.data.antoher.EmployeeListWrapper
import com.kazbekov.invent.main.trusted_user_and_admin.employee.list.common.EmployeeAdapter

class SessionConfigureEmployeeFragment : Fragment() {
    private var _binding: FragmentSessionConfigureEmployeeBinding? = null
    private val binding: FragmentSessionConfigureEmployeeBinding
        get() = _binding!!

    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: SessionConfigureEmployeeViewModel by viewModels()
    private val args by navArgs<SessionConfigureEmployeeFragmentArgs>()

    private var allEmployeesAdapter: EmployeeAdapter? = null
    private var selectedEmployeesAdapter: EmployeeAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val onBackPressesCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.mainFragmentTrusted, false)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressesCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSessionConfigureEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleToolbar()
        setClickListeners()
        initLists()
        observeLiveData()

        viewModel.getEmployees(configViewModel.code!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun handleToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener {
                findNavController().popBackStack(R.id.mainFragmentTrusted, false)
            }
            menu.findItem(R.id.retry).setOnMenuItemClickListener {
                viewModel.getEmployees(configViewModel.code!!)
                true
            }
        }
    }

    private fun setClickListeners() {
        binding.nextButton.setOnClickListener {
            viewModel.checkEmployeeListStatus(configViewModel.code!!) {
                val action =
                    SessionConfigureEmployeeFragmentDirections.actionSessionConfigureEmployeeFragmentToSessionConfigurePositionFragment(
                        EmployeeListWrapper(viewModel.selectedEmployees.value!!),
                        args.sessionId
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun initLists() {
        allEmployeesAdapter = EmployeeAdapter(requireContext()) {
            viewModel.attachEmployee(it)
        }
        selectedEmployeesAdapter = EmployeeAdapter(requireContext()) {
            viewModel.detachEmployee(it)
        }

        with(binding.allEmployeeList) {
            adapter = allEmployeesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
        with(binding.selectedEmployeeList) {
            adapter = selectedEmployeesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                binding.includedProgressBar.progressBar.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
            }
            allEmployees.observe(viewLifecycleOwner) {
                allEmployeesAdapter?.submitList(it.toList())
            }
            selectedEmployees.observe(viewLifecycleOwner) {
                selectedEmployeesAdapter?.submitList(it.toList())
            }
        }
    }
}