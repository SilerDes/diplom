package com.kazbekov.invent.main.trusted_user_and_admin.employee.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentSearchEmployeeBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.FailureDeletedState
import com.kazbekov.invent.main.trusted_user_and_admin.employee.deletion.DeleteEmployeeDialogFragment
import com.kazbekov.invent.main.trusted_user_and_admin.employee.modification.ModifyEmployeeFragment
import com.kazbekov.invent.main.utils.showMessage
import kotlin.random.Random

class SearchEmployeeFragment : Fragment() {

    private var _binding: FragmentSearchEmployeeBinding? = null
    private val binding: FragmentSearchEmployeeBinding
        get() = _binding!!
    private val searchViewModel: SearchEmployeeViewModel by viewModels()
    private val activityConfigViewModel: ConfigViewModel by activityViewModels()
    private var imm: InputMethodManager? = null

    private var previousDeletedEmployee: Int? = null

    private var currentStateId = Random.nextInt()
        set(value) {
            field = if (field == value) {
                if (value > 0) value - 1 else value + 1
            } else {
                value
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        _binding =
            FragmentSearchEmployeeBinding.inflate(LayoutInflater.from(requireContext()))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            previousDeletedEmployee = savedInstanceState.getInt(KEY_EMPLOYEE_CODE)
        }

        handleSearchView()
        handleToolbarNavigation()
        handleEmployeeCard()
        observeLiveData()
        observeNavigationLiveData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        previousDeletedEmployee?.let {
            outState.putInt(KEY_EMPLOYEE_CODE, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        imm = null
    }

    private fun handleSearchView() {
        binding.searchButton.setOnClickListener {
            imm!!.hideSoftInputFromWindow(
                requireActivity().currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            binding.searchViewEditText.text.toString().toIntOrNull()?.let {
                if (it != 0) {
                    searchViewModel.searchEmployee(activityConfigViewModel.code!!, it)
                } else {
                    setError(requireContext().getString(R.string.error_incorrect_input_code))
                }
            } ?: setError(requireContext().getString(R.string.error_invalid_search_value))
        }
    }

    private fun observeLiveData() {
        with(searchViewModel) {
            progress.observe(this@SearchEmployeeFragment.viewLifecycleOwner) {
                binding.includedProgressBar.progressBar.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
            }
            searchResult.observe(this@SearchEmployeeFragment.viewLifecycleOwner) {
                it ?: return@observe

                searchViewModel.currentEmployee = it
                initCard()
            }
            failure.observe(this@SearchEmployeeFragment.viewLifecycleOwner) {
                setError(it)
            }
        }

    }

    private fun observeNavigationLiveData() {
        val navigationStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        navigationStateHandle ?: return

        navigationStateHandle.getLiveData<Int>(
            DeleteEmployeeDialogFragment.KEY_SUCCESSFUL_DELETION
        ).observe(viewLifecycleOwner) {
            if (it != previousDeletedEmployee) {
                previousDeletedEmployee = it
                binding.includedEmployeeCard.employeeCard.visibility = View.INVISIBLE
                showMessage(requireView(), requireContext().getString(R.string.employee_deleted))
            }
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
                if (it != searchViewModel.currentEmployee) {
                    searchViewModel.currentEmployee = it
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
                    SearchEmployeeFragmentDirections.actionSearchEmployeeFragmentToModifyEmployeeFragment(
                        code, firstName, secondName, status
                    )
                findNavController().navigate(action)
            }

            fun delete() {
                val currentEmployee = searchViewModel.currentEmployee ?: return
                val action =
                    SearchEmployeeFragmentDirections.actionSearchEmployeeFragmentToDeleteEmployeeDialogFragment(
                        id = currentStateId,
                        code = currentEmployee.code
                    )
                findNavController().navigate(action)
            }

            editEmployee.setOnClickListener {
                val currentEmployee = searchViewModel.currentEmployee ?: return@setOnClickListener

                currentEmployee.code.let { toEdit ->
                    val by = activityConfigViewModel.statusCode
                    val status = currentEmployee.trustedStatusId
                    when {
                        activityConfigViewModel.code!! == toEdit -> {
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
                                this@SearchEmployeeFragment.requireView(),
                                requireContext().getString(R.string.server_error_403_status)
                            )
                        }
                    }
                }
            }
            deleteEmployee.setOnClickListener {
                val currentEmployee = searchViewModel.currentEmployee ?: return@setOnClickListener
                val by = activityConfigViewModel.statusCode
                val status = currentEmployee.trustedStatusId

                when {
                    //Доверенный статус
                    by == 3 -> {
                        delete()
                    }

                    by >= 2 && status == 1 -> {
                        delete()
                    }

                    else -> {
                        showMessage(
                            this@SearchEmployeeFragment.requireView(),
                            requireContext().getString(R.string.server_error_403_status)
                        )
                    }
                }
            }
        }
    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initCard() {
        with(binding.includedEmployeeCard) {
            employeeCard.visibility = View.VISIBLE
            this.code.text = searchViewModel.currentEmployee!!.code.toString()
            this.firstName.text = searchViewModel.currentEmployee!!.firstName
            this.secondName.text = searchViewModel.currentEmployee!!.secondName
            status.text = searchViewModel.currentEmployee!!.trustedStatus
        }
    }

    private fun setError(text: String) {
        showMessage(requireView(), text)
    }

    companion object {
        private const val KEY_EMPLOYEE_CODE = "previous_deleted_employee"
    }

}