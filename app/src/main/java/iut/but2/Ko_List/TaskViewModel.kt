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

    fun addTask(title: String, description: String, dueDate: Long? = null, periodicity: Periodicity = Periodicity.NONE) {
        val newTask = Task(title = title, description = description, dueDate = dueDate, periodicity = periodicity)
        _tasks.value = _tasks.value + newTask
    }

    fun updateTask(id: String, title: String, description: String, dueDate: Long? = null, periodicity: Periodicity = Periodicity.NONE) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(title = title, description = description, dueDate = dueDate, periodicity = periodicity) else task
        }
    }

    fun toggleTaskDone(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                val isDone = !task.isDone
                // Si la tâche est marquée comme faite et qu'elle est périodique,
                // on pourrait imaginer un comportement plus complexe (ex: créer la suivante).
                // Pour l'instant, on se contente de changer l'état.
                task.copy(isDone = isDone)
            } else task
        }
    }

    // --- Version 3 : Purge des tâches effectuées ---
    fun clearDoneTasks() {
        _tasks.value = _tasks.value.filter { !it.isDone }
    }

    fun getTask(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }
}
