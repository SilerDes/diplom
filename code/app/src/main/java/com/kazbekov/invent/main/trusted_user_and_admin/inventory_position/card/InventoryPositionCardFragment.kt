package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.card

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.kazbekov.invent.ConfigViewModel
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.FragmentInventoryPositionBinding
import com.kazbekov.invent.main.utils.Logger
import com.kazbekov.invent.main.utils.showMessage
import kotlin.random.Random

class InventoryPositionCardFragment : Fragment() {

    private var _binding: FragmentInventoryPositionBinding? = null
    private val binding: FragmentInventoryPositionBinding
        get() = _binding!!
    private val configViewModel: ConfigViewModel by activityViewModels()

    private val args by navArgs<InventoryPositionCardFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryPositionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initState(
            titleOfficial = args.titleOfficial,
            titleUser = args.titleUser,
            imageLink = args.imageLink
        )
        handleToolbarNavigation()
        handleToolbarMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun initState(
        imageLink: String? = null,
        titleOfficial: String,
        titleUser: String,
        imageBitmap: Bitmap? = null
    ) {
        val source: Any? = when (imageBitmap) {
            null -> {
                imageLink
            }

            else -> {
                imageBitmap
            }
        }
        binding.inventoryPositionPreview.load(source)
        binding.titleOfficial.text = titleOfficial
        binding.titleUser.text = titleUser
    }

    private fun handleToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleToolbarMenu() {
        //Редактор
        with(binding.toolbar.menu) {
            findItem(R.id.edit).setOnMenuItemClickListener {
                if (configViewModel.statusCode >= 2) {
                    val action =
                        InventoryPositionCardFragmentDirections.actionInventoryPositionCardToModifyInventoryPositionFragment(
                            args.titleOfficial,
                            args.titleUser,
                            args.positionId
                        )
                    findNavController().navigate(action)
                } else {
                    showMessage(
                        requireView(),
                        requireContext().getString(R.string.server_error_403_status)
                    )
                }
                true
            }
            findItem(R.id.delete).setOnMenuItemClickListener {
                val action =
                    InventoryPositionCardFragmentDirections.actionInventoryPositionCardToDeletePositionDialogFragment(
                        args.positionId
                    )
                findNavController().navigate(action)
                true
            }
        }
    }
}