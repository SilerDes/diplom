package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.creation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kazbekov.invent.databinding.FragmentCreateInventoryPositionBinding

class CreateInventoryPosition : Fragment() {
    private var _binding: FragmentCreateInventoryPositionBinding? = null
    private val binding: FragmentCreateInventoryPositionBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateInventoryPositionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        setNavigationClickListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun setNavigationClickListener() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }
}