package com.kazbekov.invent.main.trusted_user_and_admin.session.configure.position

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.BottomDialogFragmentPickPositionsBinding
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.list.common.InventoryPositionAdapter
import com.kazbekov.invent.main.trusted_user_and_admin.session.configure.data.AttachedPositions
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.showMessage

class PickPositionsBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomDialogFragmentPickPositionsBinding? = null
    private val binding: BottomDialogFragmentPickPositionsBinding
        get() = _binding!!

    private val args by navArgs<PickPositionsBottomDialogFragmentArgs>()
    private val viewModel: PickPositionViewModel by viewModels()

    private var fullPositionsListAdapter: InventoryPositionAdapter? = null
    private var selectedPositionsListAdapter: InventoryPositionAdapter? = null

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = BottomDialogFragmentPickPositionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            if (args.needRequestPositions) {
                viewModel.getInventoryPositions()
            } else {
                viewModel.setInventoryPositionList(args.employee2positions.allPositions)
            }
        }

        viewModel.setSelectedInventoryPositionList(args.employee2positions.selectedPositions)

        initLists()
        setClickListeners()
        observeLiveData()
    }

    override fun onStart() {
        super.onStart()

        dialog!!.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.peekHeight = requireContext().resources.displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        fullPositionsListAdapter = null
        selectedPositionsListAdapter = null
    }

    private fun initLists() {
        fullPositionsListAdapter = InventoryPositionAdapter {
            viewModel.attachPosition(it)
        }
        selectedPositionsListAdapter = InventoryPositionAdapter {
            viewModel.detachPosition(it)
        }

        with(binding.availablePositionsList) {
            adapter = fullPositionsListAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }

        with(binding.selectedPositionsList) {
            adapter = selectedPositionsListAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun setClickListeners() {
        binding.pickButton.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                KEY_SELECTED_POSITIONS,
                AttachedPositions(
                    args.stateId,
                    args.employee2positions.employee,
                    viewModel.selectedPositionList.value!!,
                    viewModel.fullPositionList.value!!
                )
            )
            Logger.d(
                "PickPositionsBottomDialogFragment",
                "${viewModel.selectedPositionList.value!!}"
            )
            findNavController().popBackStack()

        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
            }

            fullPositionList.observe(viewLifecycleOwner) {
                fullPositionsListAdapter?.submitList(it.toList())
            }

            selectedPositionList.observe(viewLifecycleOwner) {
                selectedPositionsListAdapter?.submitList(it.toList())
            }

            onFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
            }
        }
    }

    companion object {
        const val KEY_SELECTED_POSITIONS = "key.selected_positions"
    }
}