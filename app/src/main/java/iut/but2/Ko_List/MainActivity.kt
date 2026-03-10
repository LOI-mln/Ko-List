@file:OptIn(ExperimentalMaterial3Api::class)
package iut.but2.Ko_List

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import iut.but2.Ko_List.ui.theme.AccentCoral
import iut.but2.Ko_List.ui.theme.MyApplicationTheme
import iut.but2.Ko_List.ui.theme.SecondaryCyan
import iut.but2.Ko_List.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val taskViewModel: TaskViewModel = viewModel()
            val levelInfo by taskViewModel.userLevelInfo.collectAsState()

            MyApplicationTheme(rankTitle = levelInfo.title) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation(taskViewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(taskViewModel: TaskViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController, 
        startDestination = "taskList",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400, easing = FastOutSlowInEasing)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.95f, animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400, easing = FastOutSlowInEasing)) }
    ) {
        composable("taskList") { TaskListScreen(navController, taskViewModel) }
        composable("addTask") { AddTaskScreen(navController, taskViewModel) }
        composable("editTask/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) EditTaskScreen(navController, taskViewModel, taskId)
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun TaskListScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    val levelInfo by viewModel.userLevelInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Objectifs", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { viewModel.clearDoneTasks() }) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Purge", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("addTask") },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter") },
                text = { Text("Nouvelle tâche", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            )
        }
    ) { padding ->
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = if (hour < 18) "Bonjour 👋" else "Bonsoir 🌙"

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp)
            )
            
            LevelHeader(levelInfo)

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                listOf(TaskFilter.ALL to "Toutes", TaskFilter.TODO to "À faire", TaskFilter.DONE to "Finies").forEach { (f, label) ->
                    FilterChip(
                        selected = currentFilter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(label, fontWeight = if (currentFilter == f) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            AnimatedContent(
                targetState = tasks.isEmpty(),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(500)) + scaleIn(initialScale = 0.95f)) togetherWith 
                    (fadeOut(animationSpec = tween(500)) + scaleOut(targetScale = 0.95f))
                },
                label = "list_empty_transition"
            ) { isEmpty ->
                if (isEmpty) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "Tout est propre !",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Profites-en pour te détendre, ou ajoute un nouvel objectif à accomplir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, start = 40.dp, end = 40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            PremiumTaskItem(
                                task = task, 
                                modifier = Modifier.animateItem(
                                    fadeInSpec = tween(400), 
                                    fadeOutSpec = tween(400), 
                                    placementSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)
                                ),
                                onClick = { navController.navigate("editTask/${task.id}") }, 
                                onCheckedChange = { viewModel.toggleTaskDone(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LevelHeader(levelInfo: LevelInfo) {
    var showLevelDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary)
            .clickable { showLevelDetails = true },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
        )) {
            Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(64.dp), 
                    shape = CircleShape, 
                    color = Color.White.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = levelInfo.level.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NIVEAU ACTUEL", 
                        style = MaterialTheme.typography.labelMedium, 
                        letterSpacing = 2.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = levelInfo.title, 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = levelInfo.xpInCurrentLevel.toFloat() / levelInfo.xpRequiredForNextLevel.toFloat(),
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    Text(
                        "${levelInfo.xpInCurrentLevel} / ${levelInfo.xpRequiredForNextLevel} XP", 
                        style = MaterialTheme.typography.labelMedium, 
                        modifier = Modifier.padding(top = 6.dp).align(Alignment.End),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }

    if (showLevelDetails) {
        AlertDialog(
            onDismissRequest = { showLevelDetails = false },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents, 
                        contentDescription = null, 
                        modifier = Modifier.size(56.dp).padding(bottom = 8.dp), 
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Rangs Honorifiques", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Gravissez les échelons et débloquez de nouveaux symboles de prestige en gagnant de l'expérience !", 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    LevelTitleItem(range = "Niveaux 1 à 4", title = "Novice de l'Organisation", currentLevel = levelInfo.level, minLvl = 1, maxLvl = 4)
                    LevelTitleItem(range = "Niveaux 5 à 9", title = "Apprenti Planificateur", currentLevel = levelInfo.level, minLvl = 5, maxLvl = 9)
                    LevelTitleItem(range = "Niveaux 10 à 19", title = "Expert en Productivité", currentLevel = levelInfo.level, minLvl = 10, maxLvl = 19)
                    LevelTitleItem(range = "Niveaux 20 à 34", title = "Maître des Tâches", currentLevel = levelInfo.level, minLvl = 20, maxLvl = 34)
                    LevelTitleItem(range = "Niveau 35 et plus", title = "Légende du Temps", currentLevel = levelInfo.level, minLvl = 35, maxLvl = 999)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLevelDetails = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Text("Fermer", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun LevelTitleItem(range: String, title: String, currentLevel: Int, minLvl: Int, maxLvl: Int) {
    val isCurrent = currentLevel in minLvl..maxLvl
    val isPassed = currentLevel > maxLvl
    
    val color = if (isCurrent) MaterialTheme.colorScheme.primary else if (isPassed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    val bgColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    val fontWeight = if (isCurrent || isPassed) FontWeight.Bold else FontWeight.Medium
    val icon = if (isCurrent) Icons.Default.Star else if (isPassed) Icons.Default.CheckCircle else Icons.Default.Lock

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(10.dp).fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                fontWeight = fontWeight, 
                color = if (isCurrent || isPassed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = range, 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isCurrent) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "En cours", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PremiumTaskItem(task: Task, modifier: Modifier = Modifier, onClick: () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> AccentCoral
        Priority.MEDIUM -> SecondaryCyan
        Priority.LOW -> Color(0xFF10B981)
    }

    val scale by animateFloatAsState(
        targetValue = if (task.isDone) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )
    val alpha by animateFloatAsState(targetValue = if (task.isDone) 0.5f else 1f, label = "alpha")

    val currentElevation by animateDpAsState(targetValue = if (task.isDone) 0.dp else 12.dp, label = "elevation")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            .alpha(alpha)
            .shadow(
                elevation = currentElevation, 
                shape = RoundedCornerShape(24.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            .clickable { onCheckedChange(!task.isDone) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        var triggerConfetti by remember { mutableStateOf(false) }
        LaunchedEffect(task.isDone) {
            if (task.isDone) {
                triggerConfetti = true
                kotlinx.coroutines.delay(1000)
                triggerConfetti = false
            }
        }

        Box {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
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
                            textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                        )
                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else null
                            )
                        }
                    
                        Row(modifier = Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (task.dueDate != null) {
                                Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(" ${SimpleDateFormat("dd MMM", Locale.FRANCE).format(Date(task.dueDate!!))}", style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            if (task.periodicity != Periodicity.NONE) {
                                Icon(imageVector = Icons.Outlined.Repeat, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                                Text(" ${task.periodicity.name}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    IconButton(onClick = onClick, modifier = Modifier.padding(start = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, 
                            contentDescription = "Éditer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (triggerConfetti) {
                ConfettiExplosion(modifier = Modifier.matchParentSize())
            }
        }
    }
}

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
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
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
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Photo")
                }
                imageUri?.let {
                    Spacer(Modifier.width(16.dp))
                    Box {
                        AsyncImage(it, null, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, modifier = Modifier.size(24.dp).align(Alignment.TopEnd).offset(8.dp, (-8).dp).background(Color.Red, CircleShape)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
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

@Composable
fun EditTaskScreen(navController: NavController, viewModel: TaskViewModel, taskId: String) {
    val task = viewModel.getTask(taskId) ?: return
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }
    var rawDate by remember { mutableStateOf(if (task.dueDate != null) SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(task.dueDate!!)) else "") }
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
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titre") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            OutlinedTextField(value = rawDate, onValueChange = { rawDate = it }, label = { Text("Échéance (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { launcher.launch("image/*") }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Changer photo")
                }
                imageUri?.let {
                    Spacer(Modifier.width(16.dp))
                    Box {
                        AsyncImage(it, null, Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        IconButton(onClick = { imageUri = null }, modifier = Modifier.size(24.dp).align(Alignment.TopEnd).offset(8.dp, (-8).dp).background(Color.Red, CircleShape)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
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

@Composable
fun ConfettiExplosion(modifier: Modifier = Modifier) {
    val particles = remember { 
        List(40) {
            val angle = Math.random() * 2 * Math.PI
            val speed = Math.random() * 400f + 100f
            Triple(
                listOf(
                    AccentCoral, 
                    SecondaryCyan, 
                    PrimaryBlue,
                    Color(0xFFFFC107), 
                    Color(0xFF10B981)
                ).random(),
                (Math.cos(angle) * speed).toFloat(),
                (Math.sin(angle) * speed).toFloat()
            )
        }
    }
    
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(1000, easing = FastOutLinearInEasing))
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        particles.forEach { (color, dx, dy) ->
            val currentX = center.x + dx * progress.value
            val currentY = center.y + dy * progress.value + (progress.value * progress.value * 300f) // gravity
            val alpha = (1f - progress.value).coerceIn(0f, 1f)
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(currentX, currentY)
            )
        }
    }
}
