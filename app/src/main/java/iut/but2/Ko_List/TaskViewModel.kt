package iut.but2.Ko_List

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        _tasks.value = _tasks.value + newTask
    }

    fun updateTask(id: String, title: String, description: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(title = title, description = description) else task
        }
    }

    fun getTask(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }
}
