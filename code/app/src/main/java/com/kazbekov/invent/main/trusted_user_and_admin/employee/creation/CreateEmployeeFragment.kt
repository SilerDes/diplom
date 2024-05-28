package com.kazbekov.invent.main.trusted_user_and_admin.employee.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentCreateEmployeeBinding
import com.kazbekov.invent.main.utils.showMessage

class CreateEmployeeFragment : Fragment() {
    private var _binding: FragmentCreateEmployeeBinding? = null
    private val binding: FragmentCreateEmployeeBinding get() = _binding!!
    private val viewModel: CreationEmployeeViewModel by viewModels()
    private val activityConfigViewModel: ConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trustedStatusItems = arrayOf(
            getString(R.string.trusted_status_user_full),
            getString(R.string.trusted_status_admin_remote),
            getString(R.string.trusted_status_god)
        )
        val exposedMenuAdapter =
            ArrayAdapter(requireContext(), R.layout.trusted_status_item, trustedStatusItems)
        (binding.inputLayoutTrustedStatus.editText as? AutoCompleteTextView)?.apply {
            setAdapter(exposedMenuAdapter)
            this.listSelection = 0
        }

        binding.inputLayoutTrustedStatus.visibility =
            if (activityConfigViewModel.statusCode <= 1) View.GONE else View.VISIBLE

        observeLiveData()
    }

    override fun onStart() {
        super.onStart()

        handleToolbarNavigation()
        setSignUpListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun observeLiveData() {
        viewModel.onSuccessful.observe(viewLifecycleOwner) {
            changeBlockInputState(false)
            showMessage(requireView(), getString(R.string.user_created), true)
            clearData()
        }

        viewModel.onFailure.observe(viewLifecycleOwner) {
            changeBlockInputState(false)
            showMessage(requireView(), it.error)
        }

    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setSignUpListener() {
        binding.putUserButton.setOnClickListener {
            val trustedStatusId =
                when (binding.inputLayoutTrustedStatus.editText!!.text.toString()) {
                    getString(R.string.trusted_status_user_full) -> {
                        0
                    }
                    getString(R.string.trusted_status_admin_remote) -> {
                        1
                    }
                    getString(R.string.trusted_status_god) -> {
                        2
                    }
                    else -> 0
                }
            singUp(
                code = binding.inputLayoutCode.editText!!.text.toString().toIntOrNull(),
                password = binding.inputLayoutPassword.editText!!.text.toString().trim(),
                firstName = binding.inputLayoutFirstName.editText!!.text.toString().trim(),
                lastName = binding.inputLayoutSecondName.editText!!.text.toString().trim(),
                trustedStatus = trustedStatusId
            )
        }
    }

    private fun singUp(
        code: Int?,
        password: String,
        firstName: String,
        lastName: String,
        trustedStatus: Int = 0,
    ) {
        when {
            code == null -> {
                showMessage(requireView(), getString(R.string.error_input_code_null))
                return
            }
            password.isEmpty() || password.isBlank() -> {
                showMessage(requireView(), getString(R.string.error_input_password_blank))
                return
            }
            firstName.isEmpty() || firstName.isBlank() -> {
                showMessage(requireView(), getString(R.string.error_input_first_name_blank))
                return
            }
            lastName.isEmpty() || lastName.isBlank() -> {
                showMessage(requireView(), getString(R.string.error_input_second_name_blank))
                return
            }
        }
        changeBlockInputState(true)
        viewModel.signUp(
            code!!,
            password.trim(),
            firstName.trim(),
            lastName.trim(),
            trustedStatus,
            activityConfigViewModel.code!!
        )
    }

    private fun changeBlockInputState(blocked: Boolean) {
        with(binding) {
            progressBar.visibility = if (blocked) View.VISIBLE else View.GONE
            putUserButton.isEnabled = !blocked
            inputLayoutCode.isEnabled = !blocked
            inputLayoutPassword.isEnabled = !blocked
            inputLayoutFirstName.isEnabled = !blocked
            inputLayoutSecondName.isEnabled = !blocked
            inputLayoutTrustedStatus.isEnabled = !blocked
        }
    }

    private fun clearData() {
        with(binding) {
            inputLayoutCode.editText!!.setText("")
            inputLayoutPassword.editText!!.setText("")
            inputLayoutFirstName.editText!!.setText("")
            inputLayoutSecondName.editText!!.setText("")
            (inputLayoutTrustedStatus.editText as? AutoCompleteTextView)?.listSelection = 0
        }
    }
}