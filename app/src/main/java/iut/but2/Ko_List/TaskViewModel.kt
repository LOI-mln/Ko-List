package iut.but2.Ko_List

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    fun addTask(title: String, description: String) {
        val newTask = Task(title, description)
        _tasks.value = _tasks.value + newTask
    }
}
