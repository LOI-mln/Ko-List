package iut.but2.Ko_List

import java.util.UUID

enum class Periodicity { NONE, DAILY, WEEKLY, MONTHLY }
enum class Priority(val label: String, val level: Int) { LOW("Basse", 1), MEDIUM("Moyenne", 2), HIGH("Haute", 3) }
enum class TaskFilter { ALL, TODO, DONE }

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val periodicity: Periodicity = Periodicity.NONE,
    val priority: Priority = Priority.MEDIUM,
    val imageUri: String? = null
)
