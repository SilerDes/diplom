package com.kazbekov.invent.main.trusted_user_and_admin.inventory_item.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kazbekov.invent.databinding.ItemInventoryItemCountBinding
import com.kazbekov.invent.main.data.inventory_item.RemoteItem

class ItemAdapter(private val onDeleteItem: (itemId: Int) -> Unit) :
    ListAdapter<RemoteItem, ItemAdapter.ItemViewHolder>(ItemDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemInventoryItemCountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ItemViewHolder(binding: ItemInventoryItemCountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val inventoryPositionTitle: TextView = binding.inventoryPositionTitle
        private val inventoryPositionCount: TextView = binding.inventoryPositionCount
        private val deleteInventoryItem = binding.deleteInventoryItemButton

        init {
            deleteInventoryItem.setOnClickListener {
                onDeleteItem(currentList[adapterPosition].id)
            }
        }

        fun bind(item: RemoteItem) {
            inventoryPositionTitle.text = item.position.titleOfficial
            inventoryPositionCount.text = item.count.toString()
        }
    }

    class ItemDiffUtil : DiffUtil.ItemCallback<RemoteItem>() {
        override fun areItemsTheSame(oldItem: RemoteItem, newItem: RemoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RemoteItem, newItem: RemoteItem): Boolean {
            return oldItem == newItem
        }
    }
}