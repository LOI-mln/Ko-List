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

    // Calcul du niveau (RPG Style)
    // Progression plus douce : Base 100, +50 XP requis par niveau supplémentaire.
    // Niveau 1 -> 2 : 100 XP
    // Niveau 2 -> 3 : 150 XP
    // Niveau 3 -> 4 : 200 XP
    val userLevelInfo: StateFlow<LevelInfo> = _userXp.map { xp ->
        var currentLevel = 1
        var xpRequiredForCurrentLevel = 100
        var xpAccumulatedForPreviousLevels = 0

        var remainingXp = xp
        while (remainingXp >= xpRequiredForCurrentLevel) {
            remainingXp -= xpRequiredForCurrentLevel
            xpAccumulatedForPreviousLevels += xpRequiredForCurrentLevel
            currentLevel++
            xpRequiredForCurrentLevel += 50 // Augmentation linéaire au lieu d'exponentielle
        }

        val title = when (currentLevel) {
            in 1..4 -> "Novice de l'Organisation"
            in 5..9 -> "Apprenti Planificateur"
            in 10..19 -> "Expert en Productivité"
            in 20..34 -> "Maître des Tâches"
            else -> "Légende du Temps"
        }

        LevelInfo(
            level = currentLevel,
            title = title,
            xpInCurrentLevel = remainingXp, // XP purely in the current bracket
            xpRequiredForNextLevel = xpRequiredForCurrentLevel // XP total required IN this bracket
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LevelInfo(1, "Novice de l'Organisation", 0, 100))

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
                var newHasGrantedXp = task.hasGrantedXp
                if (newStatus && !task.hasGrantedXp) {
                    val xpGained = when(task.priority) {
                        Priority.HIGH -> 100
                        Priority.MEDIUM -> 50
                        Priority.LOW -> 25
                    }
                    _userXp.value += xpGained
                    newHasGrantedXp = true
                }
                task.copy(isDone = newStatus, hasGrantedXp = newHasGrantedXp)
            } else task
        }
    }

    fun clearDoneTasks() { _tasks.value = _tasks.value.filter { !it.isDone } }

    fun removeTask(id: String) { _tasks.value = _tasks.value.filter { it.id != id } }

    fun getTask(id: String): Task? = _tasks.value.find { it.id == id }
}

data class LevelInfo(
    val level: Int,
    val title: String,
    val xpInCurrentLevel: Int,
    val xpRequiredForNextLevel: Int
)
