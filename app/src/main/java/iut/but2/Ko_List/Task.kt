package iut.but2.Ko_List

import java.util.UUID

enum class Periodicity {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String, 
    val description: String,
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val periodicity: Periodicity = Periodicity.NONE
)

enum class TaskFilter {
    ALL,
    TODO,
    DONE
}
