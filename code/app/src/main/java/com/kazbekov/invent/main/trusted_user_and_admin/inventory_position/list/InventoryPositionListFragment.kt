package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kazbekov.invent.databinding.FragmentInventoryPositionListBinding
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.list.common.InventoryPositionAdapter
import com.kazbekov.invent.main.utils.showMessage

class InventoryPositionListFragment : Fragment() {
    private var _binding: FragmentInventoryPositionListBinding? = null
    private val binding: FragmentInventoryPositionListBinding
        get() = _binding!!
    private val viewModel: InventoryPositionViewModel by viewModels()
    private var inventoryPositionAdapter: InventoryPositionAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryPositionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleToolbarNavigation()
        observerLiveData()
        initList()
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getPositions()
        }
        viewModel.getPositions()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        inventoryPositionAdapter = null
    }

    private fun observerLiveData() {
        with(viewModel) {
            progress.observe(viewLifecycleOwner) { inProgress ->
                binding.refreshLayout.isRefreshing = inProgress
            }
            sourceSet.observe(viewLifecycleOwner) {
                inventoryPositionAdapter?.submitList(it)
            }
            failure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
            }
        }
    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initList() {
        inventoryPositionAdapter = InventoryPositionAdapter {
            openInventoryPositionCard(it)
        }
        with(binding.inventoryPositions) {
            adapter = inventoryPositionAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun openInventoryPositionCard(position: RemoteInventoryPosition) {
        val action =
            InventoryPositionListFragmentDirections
                .actionInventoryPositionListFragmentToInventoryPositionCard(
                    titleOfficial = position.titleOfficial,
                    titleUser = position.titleNonOfficial,
                    imageLink = position.imageLink,
                    positionId = position.id
                )
        findNavController().navigate(action)
    }
}