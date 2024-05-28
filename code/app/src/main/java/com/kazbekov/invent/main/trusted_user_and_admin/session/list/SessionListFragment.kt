package com.kazbekov.invent.main.trusted_user_and_admin.session.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentSessionListBinding
import com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.AdminInventoryFragment
import com.kazbekov.invent.main.trusted_user_and_admin.session.list.common.SessionAdapter
import com.kazbekov.invent.main.utils.showMessage
import java.util.Locale

class SessionListFragment : Fragment() {

    private var _binding: FragmentSessionListBinding? = null
    private val binding: FragmentSessionListBinding
        get() = _binding!!

    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: SessionListViewModel by viewModels()

    private val args by navArgs<SessionListFragmentArgs>()
    private var sessionAdapter: SessionAdapter? = null
    private var isFirstLaunch = true

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val onBackPressesCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (args.listType) {
                    LIST_TYPE_FOR_EMPLOYEE -> {
                        if (configViewModel.statusCode > 0) {
                            findNavController().popBackStack()
                        } else {
                            requireActivity().finish()
                        }
                    }

                    LIST_TYPE_ALL_CREATED -> findNavController().popBackStack()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressesCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSessionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            isFirstLaunch = savedInstanceState.getBoolean(KEY_FIRST_LAUNCH)
            if (isFirstLaunch) getSessions()
        } else {
            getSessions()
        }

        handleToolbar()
        initList()
        observeLiveData()
        observeNavigationLiveData()

        binding.refreshLayout.setOnRefreshListener {
            getSessions()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        isFirstLaunch = false
        outState.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        sessionAdapter = null
        _binding = null
    }

    private fun handleToolbar() {
        when (args.listType) {
            LIST_TYPE_ALL_CREATED -> R.string.title_all_sessions
            LIST_TYPE_FOR_EMPLOYEE -> R.string.title_sessions_for_me
            else -> null
        }?.let { res ->
            binding.toolbar.setTitle(res)
        }

        binding.toolbar.setNavigationOnClickListener {
            when (args.listType) {
                LIST_TYPE_FOR_EMPLOYEE -> {
                    if (configViewModel.statusCode > 0) {
                        findNavController().popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }

                LIST_TYPE_ALL_CREATED -> findNavController().popBackStack()
            }
        }
    }

    private fun initList() {
        sessionAdapter = SessionAdapter(requireContext()) {
            when (args.listType) {
                LIST_TYPE_FOR_EMPLOYEE -> {
                    val action =
                        SessionListFragmentDirections.actionSessionListFragmentToUserInventoryFragment(
                            it.id
                        )
                    findNavController().navigate(action)
                }

                LIST_TYPE_ALL_CREATED -> {
                    val action =
                        SessionListFragmentDirections.actionSessionListFragmentToAdminInventoryFragment(
                            it
                        )
                    findNavController().navigate(action)
                }
            }
        }
        with(binding.sessionsList) {
            adapter = sessionAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            setHasFixedSize(true)
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                binding.refreshLayout.isRefreshing = it
            }
            onSuccessful.observe(viewLifecycleOwner) {
                sessionAdapter?.submitList(it.sessions)
            }
            onFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
            }
        }
    }

    private fun observeNavigationLiveData() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(
            AdminInventoryFragment.KEY_DELETED_SESSION
        )?.observe(viewLifecycleOwner) {
            viewModel.putAwaySession(it)
        }
    }

    private fun getSessions() {
        when (args.listType) {
            LIST_TYPE_ALL_CREATED -> viewModel.getAllCreatedSessions(configViewModel.code!!)
            LIST_TYPE_FOR_EMPLOYEE -> viewModel.getUserSessions(configViewModel.code!!)
        }
    }

    companion object {
        private const val KEY_FIRST_LAUNCH = "key.first_launch"

        const val LIST_TYPE_ALL_CREATED = "type.all"
        const val LIST_TYPE_FOR_EMPLOYEE = "type.for_employee"
    }
}