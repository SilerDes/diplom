package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.modification

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentModifyPositionBinding
import com.kazbekov.invent.main.utils.showMessage
import java.io.File

class ModifyInventoryPositionFragment : Fragment() {
    private var _binding: FragmentModifyPositionBinding? = null
    private val binding: FragmentModifyPositionBinding
        get() = _binding!!

    private var imm: InputMethodManager? = null

    private val args by navArgs<ModifyInventoryPositionFragmentArgs>()
    private val configViewModel: ConfigViewModel by activityViewModels()
    private val viewModel: ModifyInventoryPositionViewModel by viewModels()

    private var isUpdated = false
    private var tempImageUri: Uri? = null
    private var cameraManager: CameraManager? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
            if (imageUri != null) {
                saveNewImage(imageUri)
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
            if (isSaved) {
                saveNewImage(tempImageUri!!)
            }
        }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showRationale()
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val onBackPressesCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isUpdated) {
                    findNavController().popBackStack(R.id.inventoryPositionListFragment, false)
                } else {
                    findNavController().popBackStack()
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
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _binding = FragmentModifyPositionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            isUpdated = savedInstanceState.getBoolean(KEY_UPDATED)
        }

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraManager?.let { cm ->
            if (cm.cameraIdList.isEmpty()) {
                binding.takePhotoButton.isEnabled = false
            }
        } ?: run { binding.takePhotoButton.isEnabled = false }

        initState()
        handleToolbarNavigation()
        handleToolbarMenu()
        addTextChangeListeners()
        setClickListeners()
        observeLiveData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(KEY_UPDATED, isUpdated)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        imm = null
        _binding = null
    }

    private fun updateInventoryPosition(
        titleOfficial: String,
        titleUser: String
    ) {
        when {
            titleOfficial.isEmpty() || titleOfficial.isBlank() -> {
                showMessage(
                    requireView(),
                    requireContext().getString(R.string.error_input_official_title_blank)
                )
            }

            titleUser.isEmpty() || titleUser.isBlank() -> {
                showMessage(
                    requireView(),
                    requireContext().getString(R.string.error_input_non_official_title_blank)
                )
            }

            else -> {
                if (!viewModel.progress.value!!) {
                    viewModel.updatePosition(
                        configViewModel.code!!,
                        args.positionId,
                        titleOfficial,
                        titleUser
                    )
                } else {
                    showMessage(
                        requireView(),
                        requireContext().getString(R.string.error_request_already_in_progress)
                    )
                }
            }
        }
    }

    private fun saveNewImage(uri: Uri) {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> {
                MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    uri
                )
            }

            else -> {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        viewModel.setSelectedBitmap(bitmap)
    }

    private fun pickPhoto() {
        getContent.launch(MIMETYPE_IMAGE)
    }

    private fun takePhoto() {
        if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            openCamera()
        }
    }

    private fun initState() {
        with(binding) {
            titleOfficialEditText.setText(args.titleOfficial)
            titleUserEditText.setText(args.titleUser)
        }
    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            if (isUpdated) {
                findNavController().popBackStack(R.id.inventoryPositionListFragment, false)
            } else {
                findNavController().popBackStack()
            }
        }
    }

    private fun handleToolbarMenu() {
        binding.toolbar.menu.findItem(R.id.save_changes).setOnMenuItemClickListener {
            imm!!.hideSoftInputFromWindow(
                requireActivity().currentFocus?.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            binding.titleOfficialEditText.clearFocus()
            binding.titleUserEditText.clearFocus()

            updateInventoryPosition(
                binding.titleOfficialEditText.text.toString().trim(),
                binding.titleUserEditText.text.toString().trim()
            )

            true
        }
    }

    private fun addTextChangeListeners() {
        with(binding) {
            titleOfficialEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    officialCounter.text = (s?.length ?: 0).toString()
                }

                override fun afterTextChanged(s: Editable?) {}

            })

            titleUserEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    userCounter.text = (s?.length ?: 0).toString()
                }

                override fun afterTextChanged(s: Editable?) {}

            })
        }
    }

    private fun setClickListeners() {
        with(binding) {
            pickPhotoButton.setOnClickListener {
                pickPhoto()
            }
            takePhotoButton.setOnClickListener {
                takePhoto()
            }
            putAwayImage.setOnClickListener {
                viewModel.removeSelectedBitmap()
            }
        }
    }

    private fun observeLiveData() {
        with(viewModel) {
            onBitmapAttached.observe(viewLifecycleOwner) { attached ->
                if (attached) {
                    binding.titlePhotoPicked.visibility = View.VISIBLE
                    binding.inventoryImage.visibility = View.VISIBLE
                    binding.putAwayImage.visibility = View.VISIBLE
                    binding.inventoryImage.setImageBitmap(viewModel.bitmap)
                } else {
                    binding.titlePhotoPicked.visibility = View.GONE
                    binding.inventoryImage.visibility = View.GONE
                    binding.putAwayImage.visibility = View.GONE
                }
            }

            progress.observe(viewLifecycleOwner) {
                binding.includedProgressBar.progressBar.visibility =
                    if (it) View.VISIBLE else View.GONE
                binding.takePhotoButton.isEnabled = !it
                binding.pickPhotoButton.isEnabled = !it
                binding.putAwayImage.isEnabled = !it
            }

            onUpdateSuccessful.observe(viewLifecycleOwner) {
                isUpdated = true
                showMessage(
                    requireView(),
                    requireContext().getString(R.string.inventory_position_updated)
                )
            }

            onUpdateFailure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it.error)
            }
        }
    }

    private fun openCamera() {
        tempImageUri =
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                createImageFile()
            )
        takePicture.launch(tempImageUri)
    }

    private fun createImageFile(): File {
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            System.currentTimeMillis().toString() + "_image",
            ".jpg",
            storageDir
        )
    }

    private fun showRationale() {
        Snackbar.make(requireView(), R.string.error_permission_denied, Snackbar.LENGTH_LONG)
            .apply {
                setAction(R.string.button_open_settings) { openSettings() }
            }
            .show()
    }

    private fun openSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(settingsIntent)
    }

    companion object {
        private const val KEY_UPDATED = "key.updated"
        private const val MIMETYPE_IMAGE = "image/*"
    }
}