package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation.result

import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentPositionPreviewBinding
import com.kazbekov.invent.main.utils.showMessage

class PositionPreviewFragment : Fragment() {
    private var _binding: FragmentPositionPreviewBinding? = null
    private val binding: FragmentPositionPreviewBinding
        get() = _binding!!
    private val viewModel: PositionPreviewViewModel by viewModels()
    private val configViewModel: ConfigViewModel by activityViewModels()

    private val navArgs: PositionPreviewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPositionPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val uri = navArgs.imageContent.uri
            viewModel.bitmap = when {
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
            itemPreview.load(viewModel.bitmap) {
                crossfade(true)
                crossfade(300)
            }
            titleOfficial.text = navArgs.titleOfficial
            titleNonOfficial.text = navArgs.titleNonOfficial
        }
    }

    override fun onStart() {
        super.onStart()

        binding.createPositionButton.setOnClickListener {
            submitPosition()
        }
        observeLiveData()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun observeLiveData() {
        with(viewModel) {
            progress.observe(viewLifecycleOwner) { inProgress ->
                binding.createPositionButton.isEnabled = !inProgress
                binding.progressBar.visibility = if (inProgress) View.VISIBLE else View.GONE
            }

            failure.observe(viewLifecycleOwner) {
                showMessage(requireView(), it)
            }

            successful.observe(viewLifecycleOwner) {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.inventory_position_created),
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack(R.id.fillHeadingsFragment, false)
            }
        }
    }

    private fun submitPosition() {
        viewModel.createInventoryPosition(
            configViewModel.code!!,
            navArgs.titleOfficial,
            navArgs.titleNonOfficial
        )
    }

}