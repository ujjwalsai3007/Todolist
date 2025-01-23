package com.example.todofinallistapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE category = :category")
    suspend fun getTasksByCategory(category: String): List<Task>

    @Insert
    suspend fun insertTask(task: Task)

    @Query("UPDATE tasks SET completed = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteTask(task: Task)

        // Get total task count by category
        @Query("SELECT COUNT(*) FROM tasks WHERE category = :category")
        suspend fun getTaskCountByCategory(category: String): Int

        // Get completed task count by category

}