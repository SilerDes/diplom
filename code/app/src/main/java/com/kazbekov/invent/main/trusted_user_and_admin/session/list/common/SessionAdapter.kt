package com.kazbekov.invent.main.trusted_user_and_admin.session.list.common

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kazbekov.invent.R
import com.kazbekov.invent.databinding.ItemSessionBinding
import com.kazbekov.invent.main.data.session.RemoteSession
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class SessionAdapter(
    private val context: Context,
    private val onSessionSelect: (RemoteSession) -> Unit
) :
    ListAdapter<RemoteSession, SessionAdapter.SessionViewHolder>(SessionDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class SessionViewHolder(
        binding: ItemSessionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        private val sessionId: TextView = binding.sessionId
        private val createdBy: TextView = binding.createdBy
        private val code: TextView = binding.code
        private val startedAt: TextView = binding.sessionStartedAt
        private val finishedAt: TextView = binding.sessionFinishedAt

        private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        init {
            binding.root.setOnClickListener {
                onSessionSelect(currentList[adapterPosition])
            }
        }

        fun bind(session: RemoteSession) {
            sessionId.text = session.id.toString()
            createdBy.text = "${session.createdBy.secondName} ${session.createdBy.firstName}"
            code.text = session.createdBy.code.toString()

            /*val startedAtDateTime = Instant.ofEpochMilli(session.startedAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
            startedAt.text = startedAtDateTime.format(formatter)

            finishedAt.text = session.finishedAt?.let {
                val finishedAtDateTime = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                finishedAtDateTime.format(formatter)
            } ?: context.getString(R.string.title_session_not_finished_yet)*/

            startedAt.text = session.startedAt
            finishedAt.text =
                session.finishedAt ?: context.getString(R.string.title_session_not_finished_yet)
        }
    }

    class SessionDiffUtilCallback : DiffUtil.ItemCallback<RemoteSession>() {
        override fun areItemsTheSame(oldItem: RemoteSession, newItem: RemoteSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RemoteSession, newItem: RemoteSession): Boolean {
            return oldItem == newItem
        }
    }
}