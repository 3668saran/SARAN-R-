package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: UserProfile,
    tasks: List<StudyTask>,
    posts: List<CommunityPost>,
    skills: List<SkillListing>,
    savedChats: List<ChatMessage>,
    onNavigate: (String) -> Unit,
    onToggleTask: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Welcome Card with Gradient
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_welcome_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryIndigo, Color(0xFF1E1B4B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Welcome back,",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = user.name,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // TrustCheck Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = when (user.status) {
                                    VerificationStatus.VERIFIED -> SuccessGreen.copy(alpha = 0.25f)
                                    VerificationStatus.PENDING -> WarningOrange.copy(alpha = 0.25f)
                                    else -> Color.White.copy(alpha = 0.15f)
                                },
                                modifier = Modifier
                                    .clickable { onNavigate("trustcheck") }
                                    .testTag("trustcheck_badge_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = when (user.status) {
                                            VerificationStatus.VERIFIED -> Icons.Filled.Verified
                                            VerificationStatus.PENDING -> Icons.Filled.HourglassEmpty
                                            else -> Icons.Outlined.Shield
                                        },
                                        contentDescription = "Status",
                                        tint = when (user.status) {
                                            VerificationStatus.VERIFIED -> Color(0xFF6EE7B7)
                                            VerificationStatus.PENDING -> Color(0xFFFDE68A)
                                            else -> Color.White
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = when (user.status) {
                                            VerificationStatus.VERIFIED -> "TrustCheck Verified"
                                            VerificationStatus.PENDING -> "Verification Pending"
                                            else -> "Verify ID"
                                        },
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatPill(icon = Icons.Filled.AutoAwesome, label = "AI Studies", value = "${savedChats.size + 4}")
                            StatPill(icon = Icons.Filled.CheckCircle, label = "Tasks Due", value = "${tasks.count { !it.isCompleted }}")
                            StatPill(icon = Icons.Filled.EmojiEvents, label = "Rep Points", value = "${user.points}")
                        }
                    }
                }
            }
        }

        // Quick Action Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Study Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        title = "Ask AI",
                        icon = Icons.Filled.Psychology,
                        modifier = Modifier.weight(1f),
                        color = PrimaryIndigo,
                        onClick = { onNavigate("assistant") }
                    )
                    QuickActionButton(
                        title = "SkillSwap",
                        icon = Icons.Filled.SwapHoriz,
                        modifier = Modifier.weight(1f),
                        color = SecondaryTeal,
                        onClick = { onNavigate("skillswap") }
                    )
                    QuickActionButton(
                        title = "Practice Quiz",
                        icon = Icons.Filled.Quiz,
                        modifier = Modifier.weight(1f),
                        color = AccentAmber,
                        onClick = { onNavigate("quizzes") }
                    )
                }
            }
        }

        // AI Assistant Prompt Teaser Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("assistant") }
                    .testTag("ai_teaser_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Gemma AI",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gemma 4 AI Study Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ask for code help, calculus derivations, or summaries",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Open AI",
                        tint = PrimaryIndigo
                    )
                }
            }
        }

        // Upcoming Study Tasks
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate("planner") }) {
                    Text("View All")
                }
            }
        }

        items(tasks.take(3)) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_item_${task.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.subject,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "• Due ${task.dueDate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PriorityBadge(priority = task.priority)
                }
            }
        }

        // Community Discussions Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Student Discussions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate("community") }) {
                    Text("Explore")
                }
            }
        }

        items(posts.take(2)) { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("community") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.subject,
                            fontSize = 12.sp,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.timestamp,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = post.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = post.content,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = "Upvotes",
                                tint = if (post.hasUpvoted) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${post.upvotes}", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${post.commentsCount}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(16.dp))
            Column {
                Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.12f),
            contentColor = color
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PriorityBadge(priority: TaskPriority) {
    val (bgColor, textColor, text) = when (priority) {
        TaskPriority.HIGH -> Triple(ErrorRed.copy(alpha = 0.15f), ErrorRed, "High")
        TaskPriority.MEDIUM -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Med")
        TaskPriority.LOW -> Triple(SecondaryTeal.copy(alpha = 0.15f), SecondaryTeal, "Low")
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
