package com.kazbekov.invent.main.trusted_user_and_admin.session.deletion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentDeleteSessionBinding

class DeleteSessionDialogFragment : DialogFragment() {
    private var _binding: FragmentDeleteSessionBinding? = null
    private val binding: FragmentDeleteSessionBinding
        get() = _binding!!

    private val viewModel: DeleteSessionViewModel by viewModels()
    private val configViewModel: ConfigViewModel by activityViewModels()

    private val args by navArgs<DeleteSessionDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonDelete.isEnabled = false
        setClickListeners()
        observeLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setClickListeners() {
        with(binding) {
            checkBoxAgreement.setOnCheckedChangeListener { _, isChecked ->
                buttonDelete.isEnabled = isChecked
            }
            buttonDelete.setOnClickListener {
                deleteInventoryItem()
            }
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                binding.includedProgressBar.progressBar.visibility =
                    if (it) View.VISIBLE else View.INVISIBLE
                binding.checkBoxAgreement.isEnabled = !it
                binding.buttonDelete.isEnabled = !it
            }
            onSuccessfulDeletion.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.session_deleted),
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack(R.id.sessionListFragment, false)
            }
            onFailureDeletion.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    it.error,
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun deleteInventoryItem() {
        viewModel.deleteSession(configViewModel.code!!, args.sessionId)
    }

    companion object {
        const val KEY_ITEM_DELETED = "key.inventory_item_deleted"
    }
}