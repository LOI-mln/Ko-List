package iut.but2.Ko_List

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import iut.but2.Ko_List.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel()
    NavHost(navController = navController, startDestination = "taskList", modifier = modifier) {
        composable("taskList") {
            TaskListScreen(navController = navController, viewModel = taskViewModel)
        }
        composable("addTask") {
            AddTaskScreen(navController = navController, viewModel = taskViewModel)
        }
        composable("editTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                EditTaskScreen(navController = navController, viewModel = taskViewModel, taskId = taskId)
            }
        }
    }
}

@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel, modifier: Modifier = Modifier) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { navController.navigate("addTask") }) {
                Text("Ajouter une tâche")
            }
            // Version 3 : Purge des tâches effectuées
            IconButton(onClick = { viewModel.clearDoneTasks() }) {
                Icon(Icons.Default.Delete, contentDescription = "Effacer terminées", tint = MaterialTheme.colorScheme.error)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            FilterButton("Toutes", currentFilter == TaskFilter.ALL) { viewModel.setFilter(TaskFilter.ALL) }
            FilterButton("À faire", currentFilter == TaskFilter.TODO) { viewModel.setFilter(TaskFilter.TODO) }
            FilterButton("Terminées", currentFilter == TaskFilter.DONE) { viewModel.setFilter(TaskFilter.DONE) }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task, 
                    onClick = { navController.navigate("editTask/${task.id}") },
                    onCheckedChange = { viewModel.toggleTaskDone(task.id) }
                )
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(text)
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit = {}, onCheckedChange: (Boolean) -> Unit = {}) {
    val isOverdue = task.dueDate != null && task.dueDate < System.currentTimeMillis() && !task.isDone

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.description,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Row {
                    if (task.dueDate != null) {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = "📅 ${dateFormat.format(Date(task.dueDate))}  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (task.periodicity != Periodicity.NONE) {
                        Text(
                            text = "🔄 ${task.periodicity.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onCheckedChange(it) }
            )
        }
    }
}

@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawDate by remember { mutableStateOf("") }
    var periodicity by remember { mutableStateOf(Periodicity.NONE) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rawDate, onValueChange = { rawDate = it }, label = { Text("Échéance (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Text("Périodicité", style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(periodicity.name)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Periodicity.values().forEach { p ->
                    DropdownMenuItem(text = { Text(p.name) }, onClick = { periodicity = p; expanded = false })
                }
            }
        }

        Button(
            onClick = {
                var parsedDate: Long? = null
                if (rawDate.isNotBlank()) {
                    try { parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(rawDate)?.time } catch (e: Exception) {}
                }
                viewModel.addTask(title, description, parsedDate, periodicity)
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Enregistrer")
        }
    }
}

@Composable
fun EditTaskScreen(navController: NavController, viewModel: TaskViewModel, taskId: String, modifier: Modifier = Modifier) {
    val task = viewModel.getTask(taskId) ?: return
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var rawDate by remember { mutableStateOf(if (task.dueDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(task.dueDate)) else "") }
    var periodicity by remember { mutableStateOf(task.periodicity) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = rawDate, onValueChange = { rawDate = it }, label = { Text("Échéance (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(periodicity.name)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                Periodicity.values().forEach { p ->
                    DropdownMenuItem(text = { Text(p.name) }, onClick = { periodicity = p; expanded = false })
                }
            }
        }

        Button(
            onClick = {
                var parsedDate: Long? = null
                if (rawDate.isNotBlank()) {
                    try { parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(rawDate)?.time } catch (e: Exception) {}
                }
                viewModel.updateTask(taskId, title, description, parsedDate, periodicity)
                navController.popBackStack()
            },
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
        ) {
            Text("Mettre à jour")
        }
    }
}
