package iut.but2.Ko_List

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import iut.but2.Ko_List.Task
import iut.but2.Ko_List.TaskViewModel

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
    val tasks by viewModel.tasks.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        Button(onClick = { navController.navigate("addTask") }, modifier = Modifier.padding(16.dp)) {
            Text("Ajouter une tâche")
        }
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(tasks) { task ->
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
fun TaskItem(task: Task, onClick: () -> Unit = {}, onCheckedChange: (Boolean) -> Unit = {}) {
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
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = task.description,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
            )
        }
        Checkbox(
            checked = task.isDone,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel, modifier: Modifier = Modifier) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            viewModel.addTask(title, description)
            navController.popBackStack()
        }) {
            Text("Enregistrer")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    MyApplicationTheme {
        TaskListScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskScreenPreview() {
    MyApplicationTheme {
        AddTaskScreen(navController = rememberNavController(), viewModel = viewModel())
    }
}

@Composable
fun EditTaskScreen(navController: NavController, viewModel: TaskViewModel, taskId: String, modifier: Modifier = Modifier) {
    val task = viewModel.getTask(taskId)
    if (task == null) {
        navController.popBackStack()
        return
    }

    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            viewModel.updateTask(taskId, title, description)
            navController.popBackStack()
        }) {
            Text("Mettre à jour")
        }
    }
}
