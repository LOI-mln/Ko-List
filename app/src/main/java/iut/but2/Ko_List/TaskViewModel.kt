package iut.but2.Ko_List

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    val filteredTasks: StateFlow<List<Task>> = combine(_tasks, _filter) { taskList, currentFilter ->
        when (currentFilter) {
            TaskFilter.ALL -> taskList
            TaskFilter.TODO -> taskList.filter { !it.isDone }
            TaskFilter.DONE -> taskList.filter { it.isDone }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(newFilter: TaskFilter) {
        _filter.value = newFilter
    }

    fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        _tasks.value = _tasks.value + newTask
    }

    fun updateTask(id: String, title: String, description: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(title = title, description = description) else task
        }
    }

    fun toggleTaskDone(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(isDone = !task.isDone) else task
        }
    }

    fun getTask(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }
}
