package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kazbekov.invent.databinding.ItemInventoryAdminItemStateBinding
import com.kazbekov.invent.main.data.inventory_item.RemoteInventoryItem

class InventoryItemStateAdapter(
    private val context: Context,
    private val onDeleteInventoryItem: (inventoryItemId: Int) -> Unit
) :
    ListAdapter<RemoteInventoryItem, InventoryItemStateAdapter.InventoryItemViewHolder>(
        InventoryItemDiffUtil()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryItemViewHolder {
        val binding = ItemInventoryAdminItemStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InventoryItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class InventoryItemViewHolder(binding: ItemInventoryAdminItemStateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val employeeName: TextView = binding.employeeName
        private val employeeCode: TextView = binding.employeeCode
        private val itemList: RecyclerView = binding.inventoryItemList

        private val itemAdapter = ItemAdapter { itemId ->
            onDeleteInventoryItem(itemId)
        }

        init {
            itemList.adapter = itemAdapter
            itemList.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemList.setHasFixedSize(true)
        }

        fun bind(item: RemoteInventoryItem) {
            employeeName.text = "${item.employee.secondName} ${item.employee.firstName}"
            employeeCode.text = item.employee.code.toString()
            itemAdapter.submitList(item.items)
        }
    }

    class InventoryItemDiffUtil : DiffUtil.ItemCallback<RemoteInventoryItem>() {
        override fun areItemsTheSame(
            oldItem: RemoteInventoryItem,
            newItem: RemoteInventoryItem
        ): Boolean {
            return oldItem.employee == newItem.employee
        }

        override fun areContentsTheSame(
            oldItem: RemoteInventoryItem,
            newItem: RemoteInventoryItem
        ): Boolean {
            return oldItem.items.joinToString(separator = "") == newItem.items.joinToString(
                separator = ""
            )
        }

    }
}