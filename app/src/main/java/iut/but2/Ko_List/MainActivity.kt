package iut.but2.Ko_List

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import iut.but2.Ko_List.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val taskViewModel: TaskViewModel = viewModel()
    NavHost(navController = navController, startDestination = "taskList") {
        composable("taskList") { TaskListScreen(navController, taskViewModel) }
        composable("addTask") { AddTaskScreen(navController, taskViewModel) }
        composable("editTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) EditTaskScreen(navController, taskViewModel, taskId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val xp by viewModel.userXp.collectAsState()
    val level by viewModel.userLevel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Objectifs", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { viewModel.clearDoneTasks() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Purge", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("addTask") },
                icon = { Icon(Icons.Default.Add, "Ajouter") },
                text = { Text("Nouvelle tâche") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Header XP Premium
            LevelHeader(level, xp)

            // Filtres stylisés
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                listOf(TaskFilter.ALL to "Toutes", TaskFilter.TODO to "À faire", TaskFilter.DONE to "Finies").forEach { (f, label) ->
                    FilterChip(
                        selected = currentFilter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(label) },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                items(tasks, key = { it.id }) { task ->
                    PremiumTaskItem(task, onClick = { navController.navigate("editTask/${task.id}") }, onCheckedChange = { viewModel.toggleTaskDone(task.id) })
                }
            }
        }
    }
}

@Composable
fun LevelHeader(level: Int, xp: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.background(
            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface))
        )) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(56.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = level.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("NIVEAU ACTUEL", style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp)
                    Text("Maître des Tâches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (xp % 100) / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    Text("${xp % 100}/100 XP pour le niveau suivant", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumTaskItem(task: Task, onClick: () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> Color(0xFFE53935)
        Priority.MEDIUM -> Color(0xFFFFB300)
        Priority.LOW -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barre de priorité sur le côté
            Box(modifier = Modifier.width(6.dp).fillMaxHeight().background(priorityColor))
            
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (task.imageUri != null) {
                    AsyncImage(
                        model = task.imageUri,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    
                    Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (task.dueDate != null) {
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp), MaterialTheme.colorScheme.primary)
                            Text(" ${SimpleDateFormat("dd MMM", Locale.FRANCE).format(Date(task.dueDate))}", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        if (task.periodicity != Periodicity.NONE) {
                            Icon(Icons.Default.Repeat, null, Modifier.size(14.dp), MaterialTheme.colorScheme.secondary)
                            Text(" ${task.periodicity.name}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: TaskViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rawDate by remember { mutableStateOf("") }
    var periodicity by remember { mutableStateOf(Periodicity.NONE) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var pExp by remember { mutableStateOf(false) }
    var prExp by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nouvel Objectif", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Quoi faire ?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Détails (optionnel)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = rawDate, onValueChange = { rawDate = it },
                label = { Text("Échéance (jj/mm/aaaa)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Photo")
                }
                imageUri?.let {
                    Spacer(Modifier.width(16.dp))
                    Box {
                        AsyncImage(it, null, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, Modifier.size(24.dp).align(Alignment.TopEnd).offset(8.dp, (-8).dp).background(Color.Red, CircleShape)) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp), Color.White)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Répétition", style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { pExp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(periodicity.name) }
                        DropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                            Periodicity.entries.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { periodicity = p; pExp = false }) }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Importance", style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { prExp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(priority.label) }
                        DropdownMenu(expanded = prExp, onDismissRequest = { prExp = false }) {
                            Priority.entries.forEach { pr -> DropdownMenuItem(text = { Text(pr.label) }, onClick = { priority = pr; prExp = false }) }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(rawDate)?.time } catch(e:Exception) { null }
                    viewModel.addTask(title, description, date, periodicity, priority, imageUri?.toString())
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("C'est parti !", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavController, viewModel: TaskViewModel, taskId: String) {
    val task = viewModel.getTask(taskId) ?: return
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var rawDate by remember { mutableStateOf(if (task.dueDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(task.dueDate)) else "") }
    var periodicity by remember { mutableStateOf(task.periodicity) }
    var priority by remember { mutableStateOf(task.priority) }
    var imageUri by remember { mutableStateOf<Uri?>(task.imageUri?.let { Uri.parse(it) }) }
    var pExp by remember { mutableStateOf(false) }
    var prExp by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Modifier", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = rawDate, onValueChange = { rawDate = it }, label = { Text("Échéance (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { launcher.launch("image/*") }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Icon(Icons.Default.PhotoCamera, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Changer photo")
                }
                imageUri?.let {
                    Spacer(Modifier.width(16.dp))
                    Box {
                        AsyncImage(it, null, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, Modifier.size(24.dp).align(Alignment.TopEnd).offset(8.dp, (-8).dp).background(Color.Red, CircleShape)) {
                            Icon(Icons.Default.Close, null, Modifier.size(16.dp), Color.White)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Répétition", style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { pExp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(periodicity.name) }
                        DropdownMenu(expanded = pExp, onDismissRequest = { pExp = false }) {
                            Periodicity.entries.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { periodicity = p; pExp = false }) }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Importance", style = MaterialTheme.typography.labelLarge)
                    Box {
                        OutlinedButton(onClick = { prExp = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(priority.label) }
                        DropdownMenu(expanded = prExp, onDismissRequest = { prExp = false }) {
                            Priority.entries.forEach { pr -> DropdownMenuItem(text = { Text(pr.label) }, onClick = { priority = pr; prExp = false }) }
                        }
                    }
                }
            }

            Button(onClick = {
                val date = try { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).parse(rawDate)?.time } catch(e:Exception) { null }
                viewModel.updateTask(taskId, title, description, date, periodicity, priority, imageUri?.toString())
                navController.popBackStack()
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                Text("Enregistrer les modifs", fontWeight = FontWeight.Bold)
            }
        }
    }
}
