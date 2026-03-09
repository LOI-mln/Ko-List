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

    // --- Système de Récompense (Points d'expérience) ---
    private val _userXp = MutableStateFlow(0)
    val userXp: StateFlow<Int> = _userXp

    val userLevel: StateFlow<Int> = combine(_userXp) { xp ->
        (xp[0] / 100) + 1 // Niveau monte tous les 100 XP
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val filteredTasks: StateFlow<List<Task>> = combine(_tasks, _filter) { taskList, currentFilter ->
        val list = when (currentFilter) {
            TaskFilter.ALL -> taskList
            TaskFilter.TODO -> taskList.filter { !it.isDone }
            TaskFilter.DONE -> taskList.filter { it.isDone }
        }
        list.sortedWith(compareByDescending<Task> { it.priority.level }.thenBy { it.dueDate ?: Long.MAX_VALUE })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(newFilter: TaskFilter) {
        _filter.value = newFilter
    }

    fun addTask(title: String, description: String, dueDate: Long? = null, periodicity: Periodicity = Periodicity.NONE, priority: Priority = Priority.MEDIUM) {
        val newTask = Task(title = title, description = description, dueDate = dueDate, periodicity = periodicity, priority = priority)
        _tasks.value = _tasks.value + newTask
    }

    fun updateTask(id: String, title: String, description: String, dueDate: Long? = null, periodicity: Periodicity = Periodicity.NONE, priority: Priority = Priority.MEDIUM) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(title = title, description = description, dueDate = dueDate, periodicity = periodicity, priority = priority) else task
        }
    }

    fun toggleTaskDone(id: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) {
                val newStatus = !task.isDone
                if (newStatus) {
                    // Gain d'XP selon la priorité
                    val xpGained = when(task.priority) {
                        Priority.HIGH -> 50
                        Priority.MEDIUM -> 20
                        Priority.LOW -> 10
                    }
                    _userXp.value += xpGained
                } else {
                    // Optionnel : Retrait d'XP si on décoche ?
                    // _userXp.value = (_userXp.value - 10).coerceAtLeast(0)
                }
                task.copy(isDone = newStatus)
            } else task
        }
    }

    fun clearDoneTasks() {
        _tasks.value = _tasks.value.filter { !it.isDone }
    }

    fun getTask(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }
}
