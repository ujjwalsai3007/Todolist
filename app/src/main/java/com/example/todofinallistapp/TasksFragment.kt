package com.example.todofinallistapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var tasksAdapter: TasksAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)

        database = AppDatabase.getDatabase(requireContext())
        taskDao = database.taskDao()

        recyclerView = view.findViewById(R.id.tasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val category = arguments?.getString("category")

        lifecycleScope.launch {
            val tasks = if (category != null) {
                taskDao.getTasksByCategory(category)
            } else {
                taskDao.getAllTasks()
            }

            tasksAdapter = TasksAdapter(
                tasks,
                onTaskCompletionChanged = { updatedTask ->
                    updateTaskCompletion(updatedTask)
                },
                onTaskDeleted = { taskToDelete ->
                    deleteTaskFromDatabase(taskToDelete)
                }
            )
            recyclerView.adapter = tasksAdapter
        }

        view.findViewById<FloatingActionButton>(R.id.addTaskFab).setOnClickListener {
            showAddTaskDialog()
        }

        return view
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.taskTitleInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.taskDescriptionInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)

        val categories = listOf("Work", "Personal", "Shopping", "Fitness")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                } else {
                    addTaskToDatabase(title, description, category)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun addTaskToDatabase(title: String, description: String, category: String) {
        val newTask = Task(0, title, description, category)

        lifecycleScope.launch {
            taskDao.insertTask(newTask)
            val updatedTasks = taskDao.getAllTasks()
            tasksAdapter.updateTasks(updatedTasks)
        }
    }

    private fun updateTaskCompletion(updatedTask: Task) {
        lifecycleScope.launch {
            taskDao.updateTaskCompletion(updatedTask.id, updatedTask.completed)
            val updatedTasks = taskDao.getAllTasks()
            tasksAdapter.updateTasks(updatedTasks)
        }
    }

    private fun deleteTaskFromDatabase(task: Task) {
        lifecycleScope.launch {
            taskDao.deleteTask(task)
            val updatedTasks = taskDao.getAllTasks()
            tasksAdapter.updateTasks(updatedTasks)
        }
    }
}