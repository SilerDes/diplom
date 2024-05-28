package com.kazbekov.invent.main.trusted_user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentMainTrustedBinding
import com.kazbekov.invent.main.trusted_user_and_admin.session.list.SessionListFragment

class MainFragmentTrusted : Fragment() {
    private var _binding: FragmentMainTrustedBinding? = null
    private val binding: FragmentMainTrustedBinding
        get() = _binding!!
    private val viewModel: MainTrustedViewModel by viewModels()
    private val activityConfigViewModel: ConfigViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val onBackPressesCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressesCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainTrustedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.profile_code).text =
            activityConfigViewModel.code.toString()
    }

    override fun onStart() {
        super.onStart()

        handleToolbarNavigation()
        handleMainView()
        setMenuClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun handleToolbarNavigation() {
        with(binding.included.toolbar) {
            setNavigationOnClickListener {
                binding.root.openDrawer(binding.navView)
            }
        }
    }

    private fun handleMainView() {
        with(binding.included) {
            selectAllSessions.setOnClickListener {
                val action =
                    MainFragmentTrustedDirections.actionMainFragmentTrustedToSessionListFragment(
                        SessionListFragment.LIST_TYPE_ALL_CREATED
                    )
                findNavController().navigate(action)
            }
            selectSessionsForMe.setOnClickListener {
                val action =
                    MainFragmentTrustedDirections.actionMainFragmentTrustedToSessionListFragment(
                        SessionListFragment.LIST_TYPE_FOR_EMPLOYEE
                    )
                findNavController().navigate(action)
            }
            createSession.setOnClickListener {
                findNavController().navigate(MainFragmentTrustedDirections.actionMainFragmentTrustedToCreateSessionFragment())
            }
        }
    }

    private fun setMenuClickListeners() {
        with(binding.navView.menu) {
            findItem(R.id.create_employee).setOnMenuItemClickListener {
                findNavController().navigate(R.id.action_mainFragmentTrusted_to_createEmployeeFragment)
                true
            }
            findItem(R.id.employee_list).setOnMenuItemClickListener {
                findNavController().navigate(R.id.action_mainFragmentTrusted_to_employeeListFragment)
                true
            }
            findItem(R.id.find_employee).setOnMenuItemClickListener {
                findNavController().navigate(R.id.action_mainFragmentTrusted_to_searchEmployeeFragment)
                true
            }
            findItem(R.id.create_inventory_position_item).setOnMenuItemClickListener {
                findNavController().navigate(R.id.action_mainFragmentTrusted_to_createInventoryPosition)
                true
            }
            findItem(R.id.inventory_position_list).setOnMenuItemClickListener {
                findNavController().navigate(R.id.action_mainFragmentTrusted_to_inventoryPositionListFragment)
                true
            }
        }
    }
}