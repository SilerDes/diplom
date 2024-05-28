package com.kazbekov.invent.main.trusted_user_and_admin.employee.modification

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentModifyEmployeeBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee
import com.kazbekov.invent.main.trusted_user_and_admin.employee.modification.common.ProgressType
import com.kazbekov.invent.main.utils.showMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ModifyEmployeeFragment : Fragment() {

    private var _binding: FragmentModifyEmployeeBinding? = null
    private val binding: FragmentModifyEmployeeBinding
        get() = _binding!!

    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: ModifyEmployeeViewModel by viewModels()

    private var vibrator: Vibrator? = null

    private val args by navArgs<ModifyEmployeeFragmentArgs>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentModifyEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.employeeStatusGroup.visibility =
            if (configViewModel.statusCode < 2 || args.code == configViewModel.code) View.GONE else View.VISIBLE

        handleToolbar()
        setInitialState()
        setClickListeners()
        observeLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        vibrator = null
    }

    private fun handleToolbar() {
        binding.toolbar.title =
            "${requireContext().getString(R.string.title_modify_employee)} ${args.code}"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setInitialState() {
        binding.employeeNameEditText.setText(args.firstName)
        binding.employeeSurnameEditText.setText(args.secondName)
        when (args.status) {
            0 -> binding.statusUserRadioButton.isChecked = true
            1 -> binding.statusAdminRadioButton.isChecked = true
            2 -> binding.statusGodRadioButton.isChecked = true
        }
    }

    private fun setClickListeners() {
        with(binding) {
            toolbar.menu.findItem(R.id.save_employee_changes).setOnMenuItemClickListener {
                if (viewModel.progress.value == ProgressType.NO_PROGRESS) {
                    val firstName = binding.employeeNameEditText.text.toString().trim()
                    val lastName = binding.employeeSurnameEditText.text.toString().trim()
                    val password = binding.passwordFormEditText.text.toString().trim()
                    val status = when (binding.employeeStatusGroup.checkedRadioButtonId) {
                        R.id.status_user_radio_button -> 0
                        R.id.status_admin_radio_button -> 1
                        R.id.status_god_radio_button -> 2
                        else -> args.status
                    }

                    if (binding.passwordFormEditText.visibility == View.GONE) {
                        //сохранить без изменения пароля
                        viewModel.updateEmployee(
                            by = configViewModel.code!!,
                            updatable = args.code,
                            firstName = firstName,
                            secondName = lastName,
                            status = status
                        )
                    } else if (password.isEmpty() || password.isBlank()) {
                        showMessage(
                            requireView(),
                            requireContext().getString(R.string.error_input_password_blank)
                        )
                    } else {
                        //сохранить с новым паролем
                        viewModel.updateEmployee(
                            by = configViewModel.code!!,
                            updatable = args.code,
                            firstName = firstName,
                            secondName = lastName,
                            status = status,
                            password = password
                        )
                    }
                }
                true
            }

            getPasswordFormButton.setOnClickListener {
                if (viewModel.progress.value == ProgressType.NO_PROGRESS) {
                    viewModel.requestPassword(configViewModel.code!!, args.code)
                } else {
                    showMessage(
                        requireView(),
                        requireContext().getString(R.string.error_request_already_in_progress)
                    )
                }
            }
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            progress.observe(viewLifecycleOwner) {
                when (it) {
                    ProgressType.NO_PROGRESS -> {
                        binding.includedProgressBar.progressBar.visibility = View.INVISIBLE
                    }

                    ProgressType.PASSWORD_REQUEST_PROGRESS -> {
                        binding.getPasswordFormButton.isEnabled = false
                        binding.includedProgressBar.progressBar.visibility = View.VISIBLE
                    }

                    ProgressType.SAVE_CHANGES_PROGRESS -> {
                        binding.includedProgressBar.progressBar.visibility = View.VISIBLE
                    }

                    else -> {
                        error("Не обработанный тип прогресса: $it")
                    }
                }
            }

            passwordSuccessful.observe(viewLifecycleOwner) {
                setVibrate(100)
                binding.getPasswordFormButton.visibility = View.GONE
                binding.passwordFormEditText.visibility = View.VISIBLE
                binding.passwordFormEditText.setText(it)
            }

            passwordFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error, true)
                setVibrate(100, 2, 50)
                binding.getPasswordFormButton.isEnabled = true
            }

            employeeUpdateFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
                setVibrate(100)
            }

            employeeUpdateSuccessful.observe(viewLifecycleOwner) {
                val status: String = when (it.status) {
                    0 -> R.string.trusted_status_user_remote
                    1 -> R.string.trusted_status_admin_remote
                    2 -> R.string.trusted_status_god_remote
                    else -> error("Не обработанный статус пользователя: ${it.status}")
                }.let { statusRes ->
                    requireContext().getString(statusRes)
                }

                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    KEY_UPDATED,
                    RemoteEmployee(
                        code = it.code,
                        firstName = it.firstName,
                        secondName = it.secondName,
                        trustedStatus = status,
                        trustedStatusId = it.status
                    )
                )
                findNavController().popBackStack()
            }
        }
    }

    private fun setVibrate(millis: Long, times: Int = 1, pause: Long = 0) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                for (i in 0 until times) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(
                            millis,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                    Thread.sleep(pause)
                }
            } else {
                for (i in 0 until times) {
                    vibrator?.vibrate(millis)
                    Thread.sleep(pause)
                }
            }
        }
    }

    companion object {
        const val KEY_UPDATED = "key updated"
    }
}