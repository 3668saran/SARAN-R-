package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommunityPost
import com.example.data.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    posts: List<CommunityPost>,
    onUpvote: (String) -> Unit,
    onCreatePost: (String, String, String) -> Unit,
    onReportPost: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Computer Science", "Mathematics", "Academic Policy", "Career")

    val filteredPosts = posts.filter { post ->
        val matchesCategory = selectedFilter == "All" || post.subject.equals(selectedFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                post.title.contains(searchQuery, ignoreCase = true) ||
                post.content.contains(searchQuery, ignoreCase = true) ||
                post.authorName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar & Guidelines Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Community",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { showGuidelinesDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Guidelines",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guidelines", fontSize = 12.sp)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search topics, questions, authors...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = PrimaryIndigo)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedFilter == cat,
                        onClick = { selectedFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Posts List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredPosts) { post ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_card_${post.id}"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Author Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (post.authorRole == UserRole.STAFF) AccentAmber.copy(alpha = 0.2f)
                                                else PrimaryIndigo.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = post.authorName.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = if (post.authorRole == UserRole.STAFF) AccentAmber else PrimaryIndigo
                                        )
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = post.authorName,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            )
                                            if (post.authorVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = "Verified",
                                                    tint = SuccessGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${if (post.authorRole == UserRole.STAFF) "Staff Faculty" else "Student"} • ${post.timestamp}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = post.subject,
                                        color = PrimaryIndigo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = post.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = post.content,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Upvote Button
                                    FilledTonalButton(
                                        onClick = { onUpvote(post.id) },
                                        shape = RoundedCornerShape(20.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (post.hasUpvoted) PrimaryIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (post.hasUpvoted) PrimaryIndigo else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (post.hasUpvoted) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                            contentDescription = "Upvote",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "${post.upvotes}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Comments Count
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = "Comments",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${post.commentsCount}", fontSize = 12.sp)
                                    }
                                }

                                // Report Action
                                IconButton(
                                    onClick = {
                                        onReportPost(post.id)
                                        Toast.makeText(context, "Post flagged for moderator review", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (post.isReported) Icons.Filled.Flag else Icons.Outlined.Flag,
                                        contentDescription = "Report",
                                        tint = if (post.isReported) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Post Dialog
    if (showCreateDialog) {
        var postTitle by remember { mutableStateOf("") }
        var postBody by remember { mutableStateOf("") }
        var postSubject by remember { mutableStateOf("Computer Science") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Ask or Share with Students") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Discussion Title") },
                        placeholder = { Text("e.g. Tips on graph traversal?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postSubject,
                        onValueChange = { postSubject = it },
                        label = { Text("Subject / Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = postBody,
                        onValueChange = { postBody = it },
                        label = { Text("Details & Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postTitle.isNotBlank() && postBody.isNotBlank()) {
                            onCreatePost(postTitle, postBody, postSubject)
                            showCreateDialog = false
                            Toast.makeText(context, "Post published to community", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    enabled = postTitle.isNotBlank() && postBody.isNotBlank()
                ) {
                    Text("Publish Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Community Guidelines Dialog
    if (showGuidelinesDialog) {
        AlertDialog(
            onDismissRequest = { showGuidelinesDialog = false },
            icon = { Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = PrimaryIndigo) },
            title = { Text("StudyMate Community Guidelines") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Academic Integrity: Never post exam answers or ask peers to write full assignments for you.", fontSize = 13.sp)
                    Text("2. Mutual Respect: Constructive feedback and respectful questions only.", fontSize = 13.sp)
                    Text("3. Privacy Protection: Never share personal student registration numbers or marksheet documents in open forum threads.", fontSize = 13.sp)
                    Text("4. Verified Moderation: Content is audited by institutional staff reviewers.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showGuidelinesDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }
}
