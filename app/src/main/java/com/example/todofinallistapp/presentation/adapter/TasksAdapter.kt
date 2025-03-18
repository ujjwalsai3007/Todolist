package com.example.todofinallistapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todofinallistapp.data.model.Task
import com.example.todofinallistapp.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TasksAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCheckedChange: (Task, Boolean) -> Unit
) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClick(getItem(position))
                }
            }

            binding.checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskCheckedChange(getItem(position), isChecked)
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                // Prevent triggering checkbox listener while binding
                checkboxTask.setOnCheckedChangeListener(null)
                checkboxTask.isChecked = task.isCompleted
                checkboxTask.setOnCheckedChangeListener { _, isChecked ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onTaskCheckedChange(getItem(position), isChecked)
                    }
                }

                textViewTitle.text = task.title
                textViewDescription.text = task.description.takeIf { it.isNotBlank() }
                    ?: "No description"

                // Format and show due date if available
                task.dueDate?.let { dueDate ->
                    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(dueDate))
                    textViewDescription.text = buildString {
                        append(textViewDescription.text)
                        append("\nDue: ")
                        append(formattedDate)
                    }
                }
            }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}