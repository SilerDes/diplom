package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.image

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentAddInventoryPositionItemBinding
import java.io.File

class AddImageFragment : Fragment() {
    private var _binding: FragmentAddInventoryPositionItemBinding? = null
    private val binding: FragmentAddInventoryPositionItemBinding
        get() = _binding!!

    private val navArgs: AddImageFragmentArgs by navArgs()

    private var tempImageUri: Uri? = null
    private var cameraManager: CameraManager? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
            if (imageUri != null) {
                startPreviewScreen(ContentUri(imageUri))
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
            if (isSaved) {
                startPreviewScreen(ContentUri(tempImageUri!!))
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddInventoryPositionItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        cameraManager?.let { cm ->
            if (cm.cameraIdList.isEmpty()) {
                binding.takePhotoButton.isEnabled = false
            }
        } ?: run { binding.takePhotoButton.isEnabled = false }

        binding.takePhotoButton.setOnClickListener {
            takePhoto()
        }
        binding.pickPhotoButton.setOnClickListener {
            pickPhoto()
        }
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun startPreviewScreen(content: ContentUri) {
        val action =
            AddImageFragmentDirections.actionAddImageFragmentToPositionPreviewFragment(
                navArgs.titleOfficial,
                navArgs.titleNonOfficial,
                content
            )
        findNavController().navigate(action)
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
        private const val MIMETYPE_IMAGE = "image/*"
    }
}