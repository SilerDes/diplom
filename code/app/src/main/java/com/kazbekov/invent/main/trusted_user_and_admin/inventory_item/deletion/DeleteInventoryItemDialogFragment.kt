package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.deletion

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
import com.kazbekov.invent.databinding.FragmentDeleteInventoryItemBinding

class DeleteInventoryItemDialogFragment : DialogFragment() {
    private var _binding: FragmentDeleteInventoryItemBinding? = null
    private val binding: FragmentDeleteInventoryItemBinding
        get() = _binding!!

    private val viewModel: DeleteInventoryItemViewModel by viewModels()
    private val configViewModel: ConfigViewModel by activityViewModels()

    private val args by navArgs<DeleteInventoryItemDialogFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteInventoryItemBinding.inflate(inflater, container, false)
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
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    KEY_ITEM_DELETED,
                    args.stateId
                )
                findNavController().popBackStack()

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
        viewModel.deleteInventoryItem(configViewModel.code!!, args.itemId)
    }

    companion object {
        const val KEY_ITEM_DELETED = "key.inventory_item_deleted"
    }
}