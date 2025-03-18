package com.example.todofinallistapp.domain.usecase

import com.example.todofinallistapp.data.model.Task
import com.example.todofinallistapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(categoryId: Long? = null): Flow<List<Task>> {
        return taskRepository.getTasks(categoryId)
    }
}