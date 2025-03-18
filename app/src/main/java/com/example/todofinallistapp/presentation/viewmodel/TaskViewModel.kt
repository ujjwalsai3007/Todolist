package com.example.todofinallistapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todofinallistapp.data.model.Task
import com.example.todofinallistapp.domain.repository.TaskRepository
import com.example.todofinallistapp.domain.usecase.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TaskViewModel"

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentCategoryId: Long? = null

    init {
        Log.d(TAG, "Initializing TaskViewModel")
        loadTasks()
    }

    fun loadTasks(categoryId: Long? = null) {
        Log.d(TAG, "Loading tasks with categoryId: $categoryId")
        currentCategoryId = categoryId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                getTasksUseCase(categoryId)
                    .catch { e ->
                        Log.e(TAG, "Error loading tasks: ${e.message}", e)
                        _error.value = e.message ?: "Error loading tasks"
                        _isLoading.value = false
                    }
                    .collect { taskList ->
                        Log.d(TAG, "Received ${taskList.size} tasks from database")
                        taskList.forEach { task ->
                            Log.d(TAG, "Task from DB: ${task.title}, ID: ${task.id}, Completed: ${task.isCompleted}")
                        }
                        _tasks.value = taskList.sortedByDescending { it.createdAt }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadTasks: ${e.message}", e)
                _error.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    fun addTask(task: Task) {
        Log.d(TAG, "Adding new task: ${task.title}")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                taskRepository.insertTask(task)
                Log.d(TAG, "Successfully added task: ${task.title}")
                loadTasks(currentCategoryId)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding task: ${e.message}", e)
                _error.value = e.message ?: "Error adding task"
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: Task) {
        Log.d(TAG, "Updating task: ${task.title}, ID: ${task.id}")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                taskRepository.updateTask(task)
                Log.d(TAG, "Successfully updated task: ${task.title}")
                loadTasks(currentCategoryId)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating task: ${e.message}", e)
                _error.value = e.message ?: "Error updating task"
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(task: Task) {
        Log.d(TAG, "Deleting task: ${task.title}, ID: ${task.id}")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                taskRepository.deleteTask(task)
                Log.d(TAG, "Successfully deleted task: ${task.title}")
                loadTasks(currentCategoryId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting task: ${e.message}", e)
                _error.value = e.message ?: "Error deleting task"
                _isLoading.value = false
            }
        }
    }
}