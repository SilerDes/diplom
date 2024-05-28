package com.kazbekov.invent.main.user

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kazbekov.invent.databinding.ItemInventoryItemStateBinding
import com.kazbekov.invent.main.data.inventory_item.RemoteItem

class UserInventoryAdapter(
    private val onItemSave: (inventoryItemId: Int, count: Int) -> Unit
) :
    ListAdapter<RemoteItem, UserInventoryAdapter.UserInventoryViewHolder>(UserInventoryDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserInventoryViewHolder {
        val binding = ItemInventoryItemStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserInventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserInventoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class UserInventoryViewHolder(binding: ItemInventoryItemStateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val inventoryPositionImage: ImageView = binding.positionImage
        private val titleOfficial: TextView = binding.titleOfficial
        private val titleUser: TextView = binding.titleUser
        private val counter: EditText = binding.counter
        private val onPlusCounter = binding.counterInc
        private val onMinusCounter = binding.counterDeinc
        private val saveButton: Button = binding.save

        init {
            saveButton.setOnClickListener {
                val currentItem = currentList[adapterPosition]
                onItemSave(currentItem.id, currentItem.count)
            }
            onPlusCounter.setOnClickListener {
                currentList[adapterPosition].count++
                updateCounter()
            }
            onMinusCounter.setOnClickListener {
                currentList[adapterPosition].count--
                updateCounter()
            }
            counter.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    currentList[adapterPosition].count = s.toString().toIntOrNull() ?: 0
                }

                override fun afterTextChanged(s: Editable?) {}

            })
        }

        fun bind(item: RemoteItem) {
            inventoryPositionImage.load(item.position.imageLink)
            titleOfficial.text = item.position.titleOfficial
            titleUser.text = item.position.titleNonOfficial
            counter.setText("${item.count}")
        }

        private fun updateCounter() {
            counter.setText("${currentList[adapterPosition].count}")
        }
    }

    class UserInventoryDiffUtil : DiffUtil.ItemCallback<RemoteItem>() {
        override fun areItemsTheSame(oldItem: RemoteItem, newItem: RemoteItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RemoteItem, newItem: RemoteItem): Boolean {
            return oldItem == newItem
        }

    }
}