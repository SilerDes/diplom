package com.kazbekov.invent.main.trusted_user_and_admin.employee.list.filter

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kazbekov.invent.R

class FilterDialogFragment : DialogFragment() {
    private lateinit var items: Array<String>
    private val itemsFlags = booleanArrayOf(true, true, true)
    private val args by navArgs<FilterDialogFragmentArgs>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        items = arrayOf(
            requireContext().resources.getString(R.string.trusted_status_user_remote),
            requireContext().resources.getString(R.string.trusted_status_admin_remote),
            requireContext().resources.getString(R.string.trusted_status_god_remote)
        )
        val receivedFilterList = args.flags.flags

        items.forEachIndexed { i, s ->
            itemsFlags[i] = s in receivedFilterList
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_filter_button)
            .setMultiChoiceItems(items, itemsFlags) { _, which, isChecked ->
                itemsFlags[which] = isChecked
            }
            .setPositiveButton(R.string.button_apply) { _, _ ->
                val flags = mutableListOf<String>()
                itemsFlags.forEachIndexed { index, b ->
                    if (b) flags.add(items[index])
                }
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    FILTER_FLAGS_KEY,
                    flags.toTypedArray()
                )
            }
            .create()
    }

    companion object {
        const val FILTER_FLAGS_KEY = "flags"
    }
}