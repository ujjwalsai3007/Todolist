package com.example.todofinallistapp.data.repository

import com.example.todofinallistapp.data.local.TaskDao
import com.example.todofinallistapp.data.model.Task
import com.example.todofinallistapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun getTasks(categoryId: Long?): Flow<List<Task>> {
        return if (categoryId != null) {
            taskDao.getTasksByCategory(categoryId)
        } else {
            taskDao.getAllTasks()
        }
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}