package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudyTask
import com.example.data.TaskPriority
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    tasks: List<StudyTask>,
    onToggleTask: (String) -> Unit,
    onAddTask: (String, String, TaskPriority, String) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Completed
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredTasks = tasks.filter { task ->
        when (selectedFilter) {
            1 -> !task.isCompleted
            2 -> task.isCompleted
            else -> true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("planner_screen")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Study Task Planner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track homework, exam prep, lab assignments, and project deadlines",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar of completion
                val completedCount = tasks.count { it.isCompleted }
                val totalCount = tasks.size
                val ratio = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Overall Study Progress", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "$completedCount of $totalCount completed (${(ratio * 100).toInt()}%)", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth(),
                            color = SuccessGreen,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedFilter,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    divider = {}
                ) {
                    Tab(selected = selectedFilter == 0, onClick = { selectedFilter = 0 }, text = { Text("All (${tasks.size})", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedFilter == 1, onClick = { selectedFilter = 1 }, text = { Text("Pending (${tasks.count { !it.isCompleted }})", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedFilter == 2, onClick = { selectedFilter = 2 }, text = { Text("Completed ($completedCount)", fontWeight = FontWeight.Bold) })
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No tasks found in this view", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                items(filteredTasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planner_task_${task.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { onToggleTask(task.id) }
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.subject,
                                        fontSize = 11.sp,
                                        color = PrimaryIndigo,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "• Due: ${task.dueDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            PriorityBadge(priority = task.priority)

                            IconButton(
                                onClick = { onDeleteTask(task.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskSubject by remember { mutableStateOf("Computer Science") }
        var taskPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
        var taskDueDate by remember { mutableStateOf("Friday, 5:00 PM") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Schedule Study Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Description") },
                        placeholder = { Text("e.g. Finish Calculus Problem Set 3") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taskSubject,
                        onValueChange = { taskSubject = it },
                        label = { Text("Course / Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taskDueDate,
                        onValueChange = { taskDueDate = it },
                        label = { Text("Due Date & Time") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TaskPriority.values().forEach { prio ->
                            FilterChip(
                                selected = taskPriority == prio,
                                onClick = { taskPriority = prio },
                                label = { Text(prio.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (taskTitle.isNotBlank()) {
                            onAddTask(taskTitle, taskSubject, taskPriority, taskDueDate)
                            showAddDialog = false
                            Toast.makeText(context, "Task added to planner", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    enabled = taskTitle.isNotBlank()
                ) {
                    Text("Add Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
