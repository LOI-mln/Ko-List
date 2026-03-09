package iut.but2.Ko_List

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class TaskViewModel : ViewModel() {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    // --- Système de Récompense (Points d'expérience) ---
    private val _userXp = MutableStateFlow(0)
    val userXp: StateFlow<Int> = _userXp

    val userLevel: StateFlow<Int> = _userXp.map { (it / 100) + 1 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val filteredTasks: StateFlow<List<Task>> = combine(_tasks, _filter) { taskList, currentFilter ->
        val list = when (currentFilter) {
            TaskFilter.ALL -> taskList
            TaskFilter.TODO -> taskList.filter { !it.isDone }
            TaskFilter.DONE -> taskList.filter { it.isDone }
        }
        list.sortedWith(compareByDescending<Task> { it.priority.level }.thenBy { it.dueDate ?: Long.MAX_VALUE })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(newFilter: TaskFilter) { _filter.value = newFilter }

    fun addTask(title: String, description: String, dueDate: Long?, periodicity: Periodicity, priority: Priority, imageUri: String?) {
        val newTask = Task(title = title, description = description, dueDate = dueDate, periodicity = periodicity, priority = priority, imageUri = imageUri)
        _tasks.value = _tasks.value + newTask
    }

    fun updateTask(id: String, title: String, description: String, dueDate: Long?, periodicity: Periodicity, priority: Priority, imageUri: String?) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(title = title, description = description, dueDate = dueDate, periodicity = periodicity, priority = priority, imageUri = imageUri) else task
        }
    }

    fun toggleTaskDone(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                val newStatus = !task.isDone
                if (newStatus) {
                    val xpGained = when(task.priority) {
                        Priority.HIGH -> 50
                        Priority.MEDIUM -> 20
                        Priority.LOW -> 10
                    }
                    _userXp.value += xpGained
                }
                task.copy(isDone = newStatus)
            } else task
        }
    }

    fun clearDoneTasks() { _tasks.value = _tasks.value.filter { !it.isDone } }

    fun getTask(id: String): Task? = _tasks.value.find { it.id == id }
}
