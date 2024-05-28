package com.kazbekov.invent.main.trusted_user_and_admin.employee.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentEmployeeListBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.list.common.EmployeeAdapter
import com.kazbekov.invent.main.trusted_user_and_admin.employee.list.filter.FilterDialogFragment
import com.kazbekov.invent.main.trusted_user_and_admin.employee.list.filter.FilterWrapper
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.showMessage

class EmployeeListFragment : Fragment() {
    private var _binding: FragmentEmployeeListBinding? = null
    private val binding: FragmentEmployeeListBinding
        get() = _binding!!
    private val viewModel: EmployeeListViewModel by viewModels()
    private val activityConfigViewModel: ConfigViewModel by activityViewModels()
    private var employeeAdapter: EmployeeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleToolbarNavigation()
        handleToolbarMenu()
        initList()
        observeLiveData()

        viewModel.getEmployees(activityConfigViewModel.code!!)

        binding.refreshLayout.setOnRefreshListener {
            viewModel.getEmployees(activityConfigViewModel.code!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        employeeAdapter = null
        _binding = null
    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleToolbarMenu() {
        binding.toolbar.menu.findItem(R.id.filter).setOnMenuItemClickListener {
            val action =
                EmployeeListFragmentDirections.actionEmployeeListFragmentToFilterDialogFragment(
                    FilterWrapper(viewModel.employeeListFilter.toList())
                )
            findNavController().navigate(action)
            true
        }
    }

    private fun initList() {
        employeeAdapter =
            EmployeeAdapter(requireContext()) {
                openEmployeeCardFragment(
                    it.code,
                    it.firstName,
                    it.secondName,
                    it.trustedStatus,
                    it.trustedStatusId
                )
            }
        with(binding.employeeList) {
            adapter = employeeAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun observeLiveData() {
        viewModel.progress.observe(viewLifecycleOwner) {
            binding.refreshLayout.isRefreshing = it
        }

        viewModel.employeeList.observe(viewLifecycleOwner) {
            Logger.d("EmployeeListViewModel", "adapter = $employeeAdapter")
            Logger.d("EmployeeListViewModel", "list = $it")
            employeeAdapter?.submitList(filterList(it))
        }

        viewModel.failure.observe(viewLifecycleOwner) {
            showMessage(requireView(), it.error, true)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Array<String>>(
            FilterDialogFragment.FILTER_FLAGS_KEY
        )?.observe(viewLifecycleOwner) {
            viewModel.employeeListFilter = it
            employeeAdapter?.submitList(filterList(viewModel.employeeList.value!!))
        }
    }

    private fun openEmployeeCardFragment(
        code: Int,
        firstName: String,
        secondName: String,
        status: String,
        statusId: Int
    ) {
        val action =
            EmployeeListFragmentDirections.actionEmployeeListFragmentToEmployeeCardFragment(
                code,
                firstName,
                secondName,
                status,
                statusId
            )
        findNavController().navigate(action)
    }

    private fun filterList(list: List<RemoteEmployee>): List<RemoteEmployee> {
        return list.filter { it.trustedStatus in viewModel.employeeListFilter }
    }
}