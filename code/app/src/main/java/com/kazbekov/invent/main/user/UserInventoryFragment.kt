package com.kazbekov.invent.main.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentUserInventoryBinding
import com.kazbekov.invent.main.utils.showMessage

class UserInventoryFragment : Fragment() {

    private var _binding: FragmentUserInventoryBinding? = null
    private val binding: FragmentUserInventoryBinding
        get() = _binding!!

    private val args by navArgs<UserInventoryFragmentArgs>()
    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: UserViewModel by viewModels()

    private var userInventoryAdapter: UserInventoryAdapter? = null
    private var progressSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressSnackbar =
            Snackbar.make(
                view,
                requireContext().getString(R.string.title_in_progress),
                Snackbar.LENGTH_INDEFINITE
            )

        handleToolbar()
        initList()
        observeLiveData()

        viewModel.getInventoryItems(configViewModel.code!!, args.sessionId)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        userInventoryAdapter = null
        progressSnackbar?.dismiss()
        progressSnackbar = null
        _binding = null
    }

    private fun handleToolbar() {
        with(binding.toolbar) {
            title = "${requireContext().getString(R.string.title_session)} ${args.sessionId}"
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun initList() {
        userInventoryAdapter = UserInventoryAdapter { id, count ->
            viewModel.updateInventoryItem(id, count)
        }
        with(binding.inventoryItemsList) {
            adapter = userInventoryAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                if (it) {
                    progressSnackbar?.show()
                } else {
                    progressSnackbar?.dismiss()
                }
            }

            onSuccessfulUpdate.observe(viewLifecycleOwner) {
                showMessage(
                    requireView(),
                    requireContext().getString(R.string.inventory_item_updated)
                )
            }

            onFailureUpdate.observe(viewLifecycleOwner) {
                if (it.code == CODE_NEGATIVE_COUNT) {
                    showMessage(
                        requireView(),
                        requireContext().getString(R.string.error_negative_item_count)
                    )
                } else {
                    showMessage(requireView(), it.error, true)
                }
            }

            onInventoryItemFetched.observe(viewLifecycleOwner) {
                userInventoryAdapter?.submitList(it)
            }
        }
    }

    companion object {
        const val CODE_NEGATIVE_COUNT = 5002
    }
}