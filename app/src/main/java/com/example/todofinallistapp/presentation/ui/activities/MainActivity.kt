package com.example.todofinallistapp.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todofinallistapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.todofinallistapp.presentation.adapter.TasksAdapter
import com.example.todofinallistapp.presentation.viewmodel.TaskViewModel
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todofinallistapp.data.model.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.LayoutInflater
import com.example.todofinallistapp.databinding.DialogAddTaskBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var tasksAdapter: TasksAdapter
    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting MainActivity")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupViews()
        setupClickListeners()
        observeTasks()
        updateUserGreeting()
        
        // Force load tasks
        viewModel.loadTasks()
        Log.d(TAG, "onCreate: Initial task load requested")
    }

    private fun updateUserGreeting() {
        auth.currentUser?.let { user ->
            val displayName = user.displayName
            if (displayName.isNullOrBlank()) {
                // If somehow the user got here without a name, redirect to name setup
                startActivity(Intent(this, NameSetupActivity::class.java))
                finish()
            } else {
                binding.tvGreeting.text = "Hey, $displayName!"
            }
        } ?: run {
            // If not logged in, redirect to sign in
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }
    }

    private fun setupViews() {
        Log.d(TAG, "setupViews: Setting up RecyclerView")
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            visibility = View.VISIBLE // Ensure RecyclerView is visible
        }
        
        tasksAdapter = TasksAdapter(
            onTaskClick = { task ->
                Log.d(TAG, "Task clicked: ${task.title}")
                showEditTaskDialog(task)
            },
            onTaskCheckedChange = { task, isCompleted ->
                Log.d(TAG, "Task checked changed: ${task.title}, completed: $isCompleted")
                viewModel.updateTask(task.copy(isCompleted = isCompleted))
            }
        )
        binding.rvTasks.adapter = tasksAdapter
    }

    private fun observeTasks() {
        Log.d(TAG, "observeTasks: Starting task observation")
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.tasks.collect { tasks ->
                    Log.d(TAG, "Received ${tasks.size} tasks")
                    tasks.forEach { task ->
                        Log.d(TAG, "Task: ${task.title}, ID: ${task.id}, Completed: ${task.isCompleted}")
                    }
                    tasksAdapter.submitList(tasks)
                    updateTaskSummary(tasks)
                    
                    // Update RecyclerView visibility
                    binding.rvTasks.visibility = if (tasks.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Log.e(TAG, "Error received: $it")
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    Log.d(TAG, "Loading state changed: $isLoading")
                    // You can add a progress indicator here if needed
                }
            }
        }
    }

    private fun updateTaskSummary(tasks: List<Task>) {
        Log.d(TAG, "updateTaskSummary: Updating summary with ${tasks.size} tasks")
        binding.tvTotalTasks.text = tasks.size.toString()
        val completedTasks = tasks.count { it.isCompleted }
        binding.tvCompletedTasks.text = completedTasks.toString()
        binding.tvPendingTasks.text = (tasks.size - completedTasks).toString()
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "Logout clicked")
            auth.signOut()
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }

        binding.fabAddTask.setOnClickListener {
            Log.d(TAG, "FAB clicked: Showing add task dialog")
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        Log.d(TAG, "showAddTaskDialog: Showing add task dialog")
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        var selectedDate: Long? = null
        
        // Setup date picker
        dialogBinding.taskDueDateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select due date")
                .build()

            datePicker.addOnPositiveButtonClickListener { timestamp ->
                selectedDate = timestamp
                dialogBinding.taskDueDateInput.setText(formatDate(timestamp))
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialog, _ ->
                val title = dialogBinding.taskTitleInput.text.toString().trim()
                val description = dialogBinding.taskDescriptionInput.text.toString().trim()
                
                if (title.isBlank()) {
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                Log.d(TAG, "Creating new task: $title")
                val task = Task(
                    title = title,
                    description = description,
                    isCompleted = false,
                    dueDate = selectedDate,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.addTask(task)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(this))
        var selectedDate = task.dueDate
        
        // Pre-fill existing data
        dialogBinding.taskTitleInput.setText(task.title)
        dialogBinding.taskDescriptionInput.setText(task.description)
        dialogBinding.taskDueDateInput.setText(task.dueDate?.let { formatDate(it) } ?: "")
        
        // Setup date picker
        dialogBinding.taskDueDateInput.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select due date")
                .setSelection(task.dueDate)
                .build()

            datePicker.addOnPositiveButtonClickListener { timestamp ->
                selectedDate = timestamp
                dialogBinding.taskDueDateInput.setText(formatDate(timestamp))
            }

            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogBinding.root)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Delete") { dialog, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTask(task)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .create()
            .apply {
                setOnShowListener { dialog ->
                    val positiveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        val title = dialogBinding.taskTitleInput.text.toString().trim()
                        val description = dialogBinding.taskDescriptionInput.text.toString().trim()
                        
                        if (title.isBlank()) {
                            dialogBinding.taskTitleInput.error = "Title is required"
                            return@setOnClickListener
                        }

                        viewModel.updateTask(task.copy(
                            title = title,
                            description = description,
                            dueDate = selectedDate,
                            updatedAt = System.currentTimeMillis()
                        ))
                        dialog.dismiss()
                    }
                }
            }
            .show()
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in
        if (auth.currentUser == null) {
            startActivity(Intent(this, SigninActivity::class.java))
            finish()
        }
    }
}