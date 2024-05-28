package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.titles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentFillHeadingsBinding
import com.kazbekov.invent.main.utils.showMessage

class FillHeadingsFragment : Fragment() {
    private var _binding: FragmentFillHeadingsBinding? = null
    private val binding: FragmentFillHeadingsBinding
        get() = _binding!!
    private val viewModel: HeadingsViewModel by viewModels()
    private val configViewModel: ConfigViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFillHeadingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setClickListeners()
        observeLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setClickListeners() {
        binding.nextButton.setOnClickListener {
            val titleOfficial =
                binding.officialInventoryPositionTitleLayout.editText!!.text.toString().trim()
            val titleNonOfficial =
                binding.nonOfficialInventoryPositionLayout.editText!!.text.toString().trim()

            when {
                titleOfficial.isEmpty() || titleOfficial.isBlank() -> {
                    showError(R.string.error_input_official_title_blank)
                    null
                }
                titleNonOfficial.isEmpty() || titleNonOfficial.isBlank() -> {
                    showError(R.string.error_input_non_official_title_blank)
                    null
                }
                titleOfficial.isEmpty() || titleOfficial.isBlank() &&
                        titleNonOfficial.isEmpty() || titleNonOfficial.isBlank() -> {
                    showError(R.string.error_input_official_and_non_official_title_blank)
                    null
                }
                else -> Unit
            }?.let {
                viewModel.checkAvailability(
                    configViewModel.code!!,
                    titleOfficial,
                    titleNonOfficial
                )
            }
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            progress.observe(viewLifecycleOwner) {
                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
                binding.nextButton.isEnabled = !it
                binding.nonOfficialInventoryPositionLayout.isEnabled = !it
                binding.officialInventoryPositionTitleLayout.isEnabled = !it
            }
            successful.observe(viewLifecycleOwner) { (titleOfficial, titleNonOfficial) ->
                //TODO next step
                val action =
                    FillHeadingsFragmentDirections.actionFillHeadingsFragmentToAddImageFragment(
                        titleOfficial,
                        titleNonOfficial
                    )
                findNavController().navigate(action)
            }
            failure.observe(viewLifecycleOwner) {
                showError(it)
            }
        }
    }

    private fun showError(@StringRes textRes: Int, isLong: Boolean = false) {
        showMessage(
            binding.root,
            resources.getString(textRes),
            isLong
        )
    }

    private fun showError(message: String, isLong: Boolean = false) {
        showMessage(
            binding.root,
            message,
            isLong
        )
    }
}