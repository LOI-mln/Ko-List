package iut.but2.Ko_List

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String, 
    val description: String
)
