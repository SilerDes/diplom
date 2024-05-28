package com.kazbekov.invent.main.trusted_user_and_admin.employee.deletion

import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.databinding.DialogFragmentDeleteEmployeeBinding
import com.kazbekov.invent.main.trusted_user_and_admin.employee.data.FailureDeletedState

class DeleteEmployeeDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentDeleteEmployeeBinding? = null
    private val binding: DialogFragmentDeleteEmployeeBinding
        get() = _binding!!

    private val viewModel: DeleteEmployeeViewModel by viewModels()
    private val configViewModel: ConfigViewModel by activityViewModels()

    private val navArgs by navArgs<DeleteEmployeeDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentDeleteEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
        observeLiveData()
        binding.buttonDelete.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setClickListeners() {
        binding.checkBoxAgreement.setOnCheckedChangeListener { _, isChecked ->
            binding.buttonDelete.isEnabled = isChecked
        }

        binding.buttonDelete.setOnClickListener {
            viewModel.deleteEmployee(configViewModel.code!!, navArgs.code)
        }
    }

    private fun observeLiveData() {

        viewModel.progress.observe(viewLifecycleOwner) {
            binding.buttonDelete.isEnabled = !it
            binding.includedProgressBar.progressBar.visibility =
                if (it) View.VISIBLE else View.INVISIBLE
        }
        viewModel.successful.observe(viewLifecycleOwner) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                KEY_SUCCESSFUL_DELETION,
                navArgs.code
            )
            findNavController().popBackStack()
        }
        viewModel.failure.observe(viewLifecycleOwner) {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                KEY_FAILURE_DELETION,
                FailureDeletedState(navArgs.id, it.error)
            )
            findNavController().popBackStack()
        }
    }

    companion object {
        const val KEY_SUCCESSFUL_DELETION = "delete_successful"
        const val KEY_FAILURE_DELETION = "delete_failure"
    }

}