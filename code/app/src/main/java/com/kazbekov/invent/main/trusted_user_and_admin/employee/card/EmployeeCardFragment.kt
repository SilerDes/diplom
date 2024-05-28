package com.kazbekov.invent.main.trusted_user_and_admin.employee.card

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentEmployeeCardBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.FailureDeletedState
import com.kazbekov.invent.main.trusted_user_and_admin.employee.deletion.DeleteEmployeeDialogFragment
import com.kazbekov.invent.main.trusted_user_and_admin.employee.modification.ModifyEmployeeFragment
import com.kazbekov.invent.main.utils.showMessage
import kotlin.random.Random

class EmployeeCardFragment : Fragment() {

    private var _binding: FragmentEmployeeCardBinding? = null
    private val binding: FragmentEmployeeCardBinding
        get() = _binding!!

    private val args by navArgs<EmployeeCardFragmentArgs>()

    private val configViewModel: ConfigViewModel by activityViewModels()

    private lateinit var currentEmployee: RemoteEmployee
    private var currentStateId = Random.nextInt()
        set(value) {
            field = if (field == value) {
                if (value > 0) value - 1 else value + 1
            } else {
                value
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        currentEmployee = RemoteEmployee(
            args.code,
            args.firstName,
            args.secondName,
            args.statusId,
            args.status
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            currentEmployee = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(
                    KEY_CURRENT_EMPLOYEE,
                    RemoteEmployee::class.java
                )!!
            } else {
                savedInstanceState.getParcelable<RemoteEmployee>(KEY_CURRENT_EMPLOYEE)!!
            }
        }

        handleToolbar()
        initCard()
        handleEmployeeCard()
        observeNavigationLiveData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        with(outState) {
            putParcelable(KEY_CURRENT_EMPLOYEE, currentEmployee)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initCard() {
        with(binding.includedEmployeeCard) {
            employeeCard.visibility = View.VISIBLE
            code.text = currentEmployee.code.toString()
            firstName.text = currentEmployee.firstName
            secondName.text = currentEmployee.secondName
            status.text = currentEmployee.trustedStatus
        }
    }

    private fun handleToolbar() {
        binding.toolbar.title =
            "${requireContext().getString(R.string.title_employee_card)} ${args.code}"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeNavigationLiveData() {
        val navigationStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        navigationStateHandle ?: return

        navigationStateHandle.getLiveData<Int>(
            DeleteEmployeeDialogFragment.KEY_SUCCESSFUL_DELETION
        ).observe(viewLifecycleOwner) {
            binding.includedEmployeeCard.employeeCard.visibility = View.INVISIBLE
            binding.onDeleted.visibility = View.VISIBLE
        }

        navigationStateHandle.getLiveData<FailureDeletedState>(DeleteEmployeeDialogFragment.KEY_FAILURE_DELETION)
            .observe(viewLifecycleOwner) {
                if (it.stateId == currentStateId) {
                    currentStateId = Random.nextInt()
                    showMessage(requireView(), it.message, true)
                }
            }

        navigationStateHandle.getLiveData<RemoteEmployee>(ModifyEmployeeFragment.KEY_UPDATED)
            .observe(viewLifecycleOwner) {
                if (it != currentEmployee) {
                    currentEmployee = it
                    initCard()
                    showMessage(
                        requireView(),
                        requireContext().getString(R.string.employee_updated)
                    )
                }
            }
    }

    private fun handleEmployeeCard() {
        with(binding.includedEmployeeCard) {

            fun next(
                code: Int,
                firstName: String,
                secondName: String,
                status: Int
            ) {
                val action =
                    EmployeeCardFragmentDirections.actionEmployeeCardFragmentToModifyEmployeeFragment(
                        code, firstName, secondName, status
                    )
                findNavController().navigate(action)
            }

            editEmployee.setOnClickListener {
                currentEmployee.code.let { toEdit ->
                    val by = configViewModel.statusCode
                    val status = currentEmployee.trustedStatusId
                    when {
                        configViewModel.code!! == toEdit -> {
                            //Изменение собственного профиля
                            next(
                                code = toEdit,
                                firstName = currentEmployee.firstName,
                                secondName = currentEmployee.secondName,
                                status = status
                            )
                        }

                        by >= 1 && status == 0 -> {
                            //Изменение пользователя администратором / доверенным аккаунтом
                            next(
                                code = toEdit,
                                firstName = currentEmployee.firstName,
                                secondName = currentEmployee.secondName,
                                status = status
                            )
                        }

                        by == 2 -> {
                            //Изменения доверенным аккаунтом
                            next(
                                code = toEdit,
                                firstName = currentEmployee.firstName,
                                secondName = currentEmployee.secondName,
                                status = status
                            )
                        }

                        else -> {
                            showMessage(
                                this@EmployeeCardFragment.requireView(),
                                requireContext().getString(R.string.server_error_403_status)
                            )
                        }
                    }
                }
            }
            deleteEmployee.setOnClickListener {
                val action =
                    EmployeeCardFragmentDirections.actionEmployeeCardFragmentToDeleteEmployeeDialogFragment(
                        id = currentStateId,
                        code = currentEmployee.code
                    )
                findNavController().navigate(action)

            }
        }
    }

    companion object {
        private const val KEY_CURRENT_EMPLOYEE = "key.employee_card_fragment.current_employee"
    }
}