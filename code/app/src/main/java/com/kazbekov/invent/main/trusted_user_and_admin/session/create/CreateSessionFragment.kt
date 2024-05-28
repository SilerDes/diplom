package com.kazbekov.invent.main.trusted_user_and_admin.session.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.databinding.FragmentCreateSessionBinding
import com.kazbekov.invent.main.utils.showMessage

class CreateSessionFragment : Fragment() {

    private var _binding: FragmentCreateSessionBinding? = null
    private val binding: FragmentCreateSessionBinding
        get() = _binding!!

    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: CreateSessionViewModel by viewModels()

    private var currentVisibilityContainerState = false
        set(value) {
            binding.onFailureCreationContainer.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCreateSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListeners()
        observeViewModel()

        if (savedInstanceState != null) {
            currentVisibilityContainerState = savedInstanceState.getBoolean(KEY_VISIBILITY_STATE)
            binding.onFailureCreationContainer.visibility =
                if (currentVisibilityContainerState) View.VISIBLE else View.GONE
        } else {
            //If first launch
            viewModel.createSession(configViewModel.code!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VISIBILITY_STATE, currentVisibilityContainerState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setClickListeners() {
        binding.retryCreateSession.setOnClickListener {
            viewModel.createSession(configViewModel.code!!)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            inProgress.observe(viewLifecycleOwner) {
                if (it) {
                    binding.progressCard.visibility = View.VISIBLE
                    currentVisibilityContainerState = false
                } else {
                    binding.progressCard.visibility = View.GONE
                }
            }

            onSuccessful.observe(viewLifecycleOwner) {
                val action =
                    CreateSessionFragmentDirections.actionCreateSessionFragmentToSessionConfigureEmployeeFragment(
                        it.id
                    )
                findNavController().navigate(action)
            }

            onFailure.observe(viewLifecycleOwner) {
                currentVisibilityContainerState = true
                showMessage(requireView(), it.error)
            }
        }
    }

    companion object {
        private const val KEY_VISIBILITY_STATE = "key.failure_container_visibility"
    }
}