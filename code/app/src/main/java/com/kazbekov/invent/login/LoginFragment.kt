package com.kazbekov.invent.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentLoginBinding
import com.kazbekov.invent.main.trusted_user_and_admin.session.list.SessionListFragment
import com.kazbekov.invent.main.utils.showMessage
import com.kazbekov.invent.network.InventResponse

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private val activityConfigViewModel: ConfigViewModel by activityViewModels()
    private var imm: InputMethodManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val packageInfo =
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        binding.appVersionTextView.text = packageInfo.versionName

    }

    override fun onStart() {
        super.onStart()

        initClickListeners()
        observeLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
        imm = null
    }

    //Initial click listeners
    private fun initClickListeners() {
        with(binding) {
            buttonLogin.setOnClickListener {
                loginButtonClickListener()
            }
        }

    }

    //Click listeners
    private fun loginButtonClickListener() {
        with(binding) {
            //Скрываем клавиатуру
            imm!!.hideSoftInputFromWindow(
                requireActivity().currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            //Скрываем фокус с полей ввода
            inputLayoutCode.clearFocus()
            inputLayoutPassword.clearFocus()

            val code = inputLayoutCode.editText!!.text.toString().toIntOrNull()
            val password = inputLayoutPassword.editText!!.text.toString()
            login(code, password)
        }
    }

    //Click listeners methods
    private fun login(code: Int?, password: String) {
        when {
            code == null && (password.isEmpty() || password.isBlank()) -> {
                showMessage(requireView(), getString(R.string.combinate_input_error))
                return
            }

            code == null -> {
                showMessage(requireView(), getString(R.string.error_input_code_null))
                return
            }

            code == 0 -> {
                showMessage(requireView(), getString(R.string.error_incorrect_input_code))
                return
            }

            password.isEmpty() || password.isBlank() -> {
                showMessage(requireView(), getString(R.string.error_input_password_blank))
                return
            }
        }

        changeBlockInputsState(true)
        changeProgressState(true)

        viewModel.login(code!!, password.trim())
    }

    //Observers
    private fun observeLiveData() {
        viewModel.auth.observe(viewLifecycleOwner) {
            when (it) {
                is InventResponse.SuccessfulLoginResponse -> {
                    onSuccessfulAuth(it)
                }

                is InventResponse.UnsuccessfulResponse -> {
                    onErrorAuth(it)
                }
            }
            changeBlockInputsState(false)
            changeProgressState(false)
        }
    }

    //Observers methods
    private fun onSuccessfulAuth(auth: InventResponse.SuccessfulLoginResponse) {
        activityConfigViewModel.code =
            binding.inputLayoutCode.editText!!.text.toString().toInt()
        activityConfigViewModel.statusCode = auth.employeeStatus
        if (activityConfigViewModel.statusCode == 1 || activityConfigViewModel.statusCode == 2) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragmentTrusted)
        } else {
            val action =
                LoginFragmentDirections.actionLoginFragmentToSessionListFragment(SessionListFragment.LIST_TYPE_FOR_EMPLOYEE)
            findNavController().navigate(action)
        }

    }

    private fun onErrorAuth(auth: InventResponse.UnsuccessfulResponse) {
        showMessage(requireView(), auth.error)
    }

    private fun changeBlockInputsState(blocked: Boolean) {
        with(binding) {
            inputLayoutCode.isEnabled = !blocked
            inputLayoutPassword.isEnabled = !blocked
            buttonLogin.isEnabled = !blocked
        }
    }

    private fun changeProgressState(isProgress: Boolean) {
        binding.progressBar.visibility = if (isProgress) View.VISIBLE else View.GONE
    }
}