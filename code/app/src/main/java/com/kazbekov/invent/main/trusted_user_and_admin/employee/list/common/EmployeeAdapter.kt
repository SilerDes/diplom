package com.kazbekov.invent.main.trusted_user_and_admin.employee.list.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.ItemEmployeeBinding
import com.kazbekov.invent.main.data.employee.RemoteEmployee

class EmployeeAdapter(
    private val context: Context,
    private val onItemSelect: (
        employee: RemoteEmployee
    ) -> Unit
) :
    ListAdapter<RemoteEmployee, EmployeeAdapter.EmployeeViewHolder>(EmployeeDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemEmployeeBinding.inflate(inflater, parent, false)

        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class EmployeeViewHolder(private val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val codeTextView: TextView = binding.code
        private val nameTextView: TextView = binding.name
        private val statusTextView: TextView = binding.status

        init {
            binding.root.setOnClickListener {
                val employee = currentList[adapterPosition]
                onItemSelect(
                    employee
                )
            }
        }

        fun bind(employee: RemoteEmployee) {
            codeTextView.text = employee.code.toString()
            nameTextView.text = "${employee.secondName} ${employee.firstName}"
            statusTextView.text = employee.trustedStatus

            val strokeColor = when (employee.trustedStatusId) {
                2 -> R.color.status_god_color
                1 -> R.color.status_admin_color
                else -> null
            }
            strokeColor?.let {
                binding.root.strokeColor = context.resources.getColor(it, null)
                statusTextView.setTextColor(context.resources.getColor(it, null))
            } ?: run {
                binding.root.strokeColor =
                    context.resources.getColor(R.color.bg_employee_card, null)
                statusTextView.setTextColor(context.resources.getColor(R.color.black, null))
            }
        }
    }

    class EmployeeDiffUtil : DiffUtil.ItemCallback<RemoteEmployee>() {
        override fun areItemsTheSame(oldItem: RemoteEmployee, newItem: RemoteEmployee): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: RemoteEmployee, newItem: RemoteEmployee): Boolean {
            return oldItem == newItem
        }

    }
}