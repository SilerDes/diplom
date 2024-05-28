package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item

import android.content.Context
import android.content.Intent
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
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentAdminInventoryBinding
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItem
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItems
import com.kazbekov.invent.main.data.inventory_item.RemoteItem
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.common.InventoryItemStateAdapter
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.deletion.DeleteInventoryItemDialogFragment
import com.kazbekov.invent.main.utils.showMessage
import kotlin.random.Random

class AdminInventoryFragment : Fragment() {

    private var _binding: FragmentAdminInventoryBinding? = null
    private val binding: FragmentAdminInventoryBinding
        get() = _binding!!

    private val args by navArgs<AdminInventoryFragmentArgs>()

    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: AdminInventoryViewModel by viewModels()

    private var inventoryAdapter: InventoryItemStateAdapter? = null
    private var isSessionStopped: Boolean = false
    private var currentStateId = Random.nextInt()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        isSessionStopped = args.session.finishedAt != null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAdminInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            isSessionStopped = savedInstanceState.getBoolean(FLAG_SESSION_STOPPED)
            currentStateId = savedInstanceState.getInt(FLAG_STATE_ID)
        }

        handleToolbar()
        setDefaultState()
        initList()
        setClickListeners()
        observeLiveData()
        observeNavigationLiveData()

        viewModel.getInventoryItems(configViewModel.code!!, args.session.id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(FLAG_SESSION_STOPPED, isSessionStopped)
        outState.putInt(FLAG_STATE_ID, currentStateId)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        inventoryAdapter = null
        _binding = null
    }

    private fun handleToolbar() {
        with(binding.toolbar) {
            title = "${requireContext().getString(R.string.title_session)} ${args.session.id}"
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            menu.findItem(R.id.send_data).setOnMenuItemClickListener {
                viewModel.onSuccessful.value?.employee2items?.let {
                    shareInventoryData(it)
                } ?: showMessage(
                    requireView(),
                    requireContext().getString(R.string.error_cannot_share_report),
                    true
                )
                true
            }
            menu.findItem(R.id.update_data).setOnMenuItemClickListener {
                viewModel.getInventoryItems(configViewModel.code!!, args.session.id)
                true
            }
            menu.findItem(R.id.delete_session).setOnMenuItemClickListener {
                val action =
                    AdminInventoryFragmentDirections.actionAdminInventoryFragmentToDeleteSessionDialogFragment(
                        args.session.id
                    )
                findNavController().navigate(action)
                true
            }
        }
    }

    private fun setDefaultState() {
        when (isSessionStopped) {
            false -> {
                binding.sessionStoppedWarningTextView.visibility = View.GONE
                binding.stopSession.visibility = View.VISIBLE
            }

            else -> {
                binding.sessionStoppedWarningTextView.visibility = View.VISIBLE
                binding.stopSession.visibility = View.GONE
            }
        }
    }

    private fun initList() {
        inventoryAdapter = InventoryItemStateAdapter(requireContext()) { inventoryItemId ->
            val action =
                AdminInventoryFragmentDirections.actionAdminInventoryFragmentToDeleteInventoryItemDialogFragment(
                    inventoryItemId, currentStateId
                )
            findNavController().navigate(action)
        }
        with(binding.inventoryItemList) {
            adapter = inventoryAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun setClickListeners() {
        binding.stopSession.setOnClickListener {
            viewModel.stopSession(configViewModel.code!!, args.session.id)
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                if (it) {
                    binding.inventoryItemList.visibility = View.GONE
                    binding.onLoadTextView.visibility = View.VISIBLE
                    if (!isSessionStopped) binding.stopSession.visibility = View.GONE
                } else {
                    binding.inventoryItemList.visibility = View.VISIBLE
                    binding.onLoadTextView.visibility = View.GONE
                    if (!isSessionStopped) binding.stopSession.visibility = View.VISIBLE
                }
            }

            onSuccessful.observe(viewLifecycleOwner) {
                inventoryAdapter?.submitList(it.employee2items)
            }

            onSessionStopped.observe(viewLifecycleOwner) {
                isSessionStopped = true
                binding.stopSession.visibility = View.GONE
                binding.sessionStoppedWarningTextView.visibility = View.VISIBLE
            }

            onInventoryItemDeleted.observe(viewLifecycleOwner) {
                viewModel.getInventoryItems(configViewModel.code!!, args.session.id)
            }

            onFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error, true)
            }
        }
    }

    private fun observeNavigationLiveData() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(
            DeleteInventoryItemDialogFragment.KEY_ITEM_DELETED
        )?.observe(viewLifecycleOwner) {
            if (it == currentStateId) {
                currentStateId = if (currentStateId > 0) currentStateId-- else currentStateId++
                viewModel.getInventoryItems(configViewModel.code!!, args.session.id)
            }
        }
    }

    private fun shareInventoryData(inventoryItems: List<RemoteInventoryItem>) {
        if (inventoryItems.isEmpty()) {
            showMessage(requireView(), requireContext().getString(R.string.error_report_is_empty))
            return
        }
        var shared = ""
        for (inventoryItem in inventoryItems) {
            for (item in inventoryItem.items) {
                shared += "${item.position.titleOfficial}: ${item.count}"
                shared += "\n"
            }
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shared)
        }
        val shareIntent = Intent.createChooser(
            sendIntent,
            requireContext().getString(R.string.title_inventory_session_report)
        )
        requireActivity().startActivity(shareIntent)
    }

    companion object {
        const val KEY_DELETED_SESSION = "key.navigation.deleted_session"
        private const val FLAG_SESSION_STOPPED = "key.session_stopped"
        private const val FLAG_STATE_ID = "key.state_id"
    }
}