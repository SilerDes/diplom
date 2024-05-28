package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.position

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentSessionConfigurePositionBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import com.kazbekov.invent.main.trusted_user_and_admin.employee.list.common.EmployeeAdapter
import com.kazbekov.invent.main.trusted_user_and_admin.session.configure.data.AttachedPositions
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.showMessage
import kotlin.random.Random

class SessionConfigurePositionFragment : Fragment() {

    private var _binding: FragmentSessionConfigurePositionBinding? = null
    private val binding: FragmentSessionConfigurePositionBinding
        get() = _binding!!

    private val args by navArgs<SessionConfigurePositionFragmentArgs>()
    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: SessionConfigurePositionViewModel by viewModels()

    private var employeeAdapter: EmployeeAdapter? = null
    private var currentStateId = Random.nextInt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSessionConfigurePositionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.setDefaultState(args.employees.employees)
        }

        handleToolbar()
        initList()
        setClickListeners()
        observeLiveData()
        observeNavigationLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun handleToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            menu.findItem(R.id.retry).setOnMenuItemClickListener {
                viewModel.syncEmployeesAndPositions(configViewModel.code!!, REQUEST_CODE_SYNC_ONLY)
                true
            }
        }
    }

    private fun initList() {
        employeeAdapter = EmployeeAdapter(requireContext()) {
            val action =
                SessionConfigurePositionFragmentDirections.actionSessionConfigurePositionFragmentToPickPositionsBottomDialogFragment(
                    viewModel.getCase(it), currentStateId, viewModel.needRequestPositions
                )
            findNavController().navigate(action)
        }
        with(binding.selectedEmployeeList) {
            adapter = employeeAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
        employeeAdapter?.submitList(viewModel.getEmployees())
    }

    private fun setClickListeners() {
        binding.finishSessionConfigure.setOnClickListener {
            viewModel.syncEmployeesAndPositions(
                configViewModel.code!!,
                REQUEST_CODE_SYNC_AND_FINISH
            )
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                binding.includedProgressBar.progressBar.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
            }

            onCheckSuccess.observe(viewLifecycleOwner) {
                when (it) {
                    REQUEST_CODE_SYNC_ONLY -> {
                        employeeAdapter?.submitList(viewModel.getEmployees())
                    }

                    REQUEST_CODE_SYNC_AND_FINISH -> {
                        employeeAdapter?.submitList(viewModel.getEmployees())
                        viewModel.postData(configViewModel.code!!, args.sessionId)
                    }
                }
            }

            onInventoryItemsCreated.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.session_successfully_configured),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack(R.id.mainFragmentTrusted, false)
            }

            onFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
            }
        }
    }

    private fun observeNavigationLiveData() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<AttachedPositions>(
            PickPositionsBottomDialogFragment.KEY_SELECTED_POSITIONS
        )
            ?.observe(viewLifecycleOwner) {
                Logger.d("PickPositionsBottomDialogFragment", "$it")
                if (currentStateId == it.stateId) {
                    currentStateId = if (currentStateId > 0) currentStateId-- else currentStateId++
                    viewModel.updateEmployeeCase(
                        it.employee,
                        it.selectedPositions,
                        it.remainingPositions
                    )
                }
            }
    }

    companion object {
        private const val REQUEST_CODE_SYNC_ONLY = 100
        private const val REQUEST_CODE_SYNC_AND_FINISH = 101
    }
}