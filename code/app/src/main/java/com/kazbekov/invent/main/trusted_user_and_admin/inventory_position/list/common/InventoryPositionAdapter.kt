package com.kazbekov.invent.main.trusted_user_and_admin.inventory_position.list.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.ItemInventoryPositionBinding
import com.kazbekov.invent.main.data.inventory_position.RemoteInventoryPosition

class InventoryPositionAdapter(
    private val onSelect: (RemoteInventoryPosition) -> Unit
) :
    ListAdapter<RemoteInventoryPosition, InventoryPositionAdapter.InventoryPositionViewHolder>(
        InventoryPositionDiffUtil()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryPositionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInventoryPositionBinding.inflate(inflater, parent, false)

        return InventoryPositionViewHolder(binding)

    }

    override fun onBindViewHolder(holder: InventoryPositionViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class InventoryPositionViewHolder(binding: ItemInventoryPositionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val preview: ImageView = binding.inventoryImage
        private val titleOfficial: TextView = binding.titleOfficial
        private val titleNonOfficial: TextView = binding.titleNonOfficial

        init {
            binding.root.setOnClickListener {

                onSelect(currentList[adapterPosition])
            }
        }

        fun bind(inventoryPosition: RemoteInventoryPosition) {
            titleOfficial.text = inventoryPosition.titleOfficial
            titleNonOfficial.text = inventoryPosition.titleNonOfficial
            preview.load(inventoryPosition.imageLink) {
                crossfade(true)
                crossfade(200)
                placeholder(R.drawable.ic_placeholder)
            }
        }
    }

    class InventoryPositionDiffUtil : DiffUtil.ItemCallback<RemoteInventoryPosition>() {
        override fun areItemsTheSame(
            oldItem: RemoteInventoryPosition,
            newItem: RemoteInventoryPosition
        ): Boolean {
            return oldItem.titleOfficial == newItem.titleOfficial
        }

        override fun areContentsTheSame(
            oldItem: RemoteInventoryPosition,
            newItem: RemoteInventoryPosition
        ): Boolean {
            return oldItem == newItem
        }

    }
}