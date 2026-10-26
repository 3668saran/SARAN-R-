package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExchangeStatus
import com.example.data.SkillExchangeRequest
import com.example.data.SkillListing
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSwapScreen(
    listings: List<SkillListing>,
    requests: List<SkillExchangeRequest>,
    onSendRequest: (String, String, String, String) -> Unit,
    onUpdateRequestStatus: (String, ExchangeStatus) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Find Peers, 1: My Exchanges
    var searchQuery by remember { mutableStateOf("") }
    var showRequestDialog by remember { mutableStateOf<SkillListing?>(null) }

    val filteredListings = listings.filter { item ->
        searchQuery.isBlank() ||
                item.teachSkills.any { it.contains(searchQuery, ignoreCase = true) } ||
                item.learnSkills.any { it.contains(searchQuery, ignoreCase = true) } ||
                item.userName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("skillswap_screen")
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SkillSwap Peer Learning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Exchange academic skills 1-on-1 with verified campus peers",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Find Peers (${filteredListings.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Exchanges (${requests.size})", fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            // Find Peers Tab
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search skill (e.g. Python, Calculus, React)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SecondaryTeal) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredListings) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("skill_listing_${item.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(SecondaryTeal.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.userName.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryTeal,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = item.userName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = item.collegeName,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Rating Pill
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AccentAmber.copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "Rating",
                                                tint = AccentAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${item.rating} (${item.completedSwaps} swaps)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentAmber
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Can Teach Section
                                Text(
                                    text = "CAN TEACH / SHARE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item.teachSkills.forEach { s ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SuccessGreen.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = s,
                                                fontSize = 11.sp,
                                                color = SuccessGreen,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Wants to Learn Section
                                Text(
                                    text = "WANTS TO LEARN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryIndigo
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item.learnSkills.forEach { s ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryIndigo.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = s,
                                                fontSize = 11.sp,
                                                color = PrimaryIndigo,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showRequestDialog = item },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                                ) {
                                    Icon(imageVector = Icons.Filled.SwapCalls, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Propose Skill Exchange", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // My Exchanges Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (requests.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No skill exchange requests yet. Browse peers to get started!")
                        }
                    }
                }

                items(requests) { req ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exchange_req_${req.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "With: ${if (req.fromUserName.contains("Alex")) req.toUserName else req.fromUserName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (req.status) {
                                        ExchangeStatus.ACCEPTED -> SuccessGreen.copy(alpha = 0.15f)
                                        ExchangeStatus.PENDING -> WarningOrange.copy(alpha = 0.15f)
                                        ExchangeStatus.COMPLETED -> InfoBlue.copy(alpha = 0.15f)
                                        ExchangeStatus.REJECTED -> ErrorRed.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = req.status.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (req.status) {
                                            ExchangeStatus.ACCEPTED -> SuccessGreen
                                            ExchangeStatus.PENDING -> WarningOrange
                                            ExchangeStatus.COMPLETED -> InfoBlue
                                            ExchangeStatus.REJECTED -> ErrorRed
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Offered: ${req.teachOffered} ⇄ Requested: ${req.learnRequested}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (req.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${req.message}\"",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons depending on state
                            if (req.status == ExchangeStatus.PENDING) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onUpdateRequestStatus(req.id, ExchangeStatus.ACCEPTED)
                                            Toast.makeText(context, "Skill swap accepted! Coordinate in chat.", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) {
                                        Text("Accept")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onUpdateRequestStatus(req.id, ExchangeStatus.REJECTED)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Decline")
                                    }
                                }
                            } else if (req.status == ExchangeStatus.ACCEPTED) {
                                Button(
                                    onClick = {
                                        onUpdateRequestStatus(req.id, ExchangeStatus.COMPLETED)
                                        Toast.makeText(context, "Exchange marked completed! +20 Rep Points.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                                ) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mark Learning Exchange Completed")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Propose Skill Exchange Dialog
    if (showRequestDialog != null) {
        val target = showRequestDialog!!
        var teachSkillInput by remember { mutableStateOf(target.learnSkills.firstOrNull() ?: "Kotlin") }
        var learnSkillInput by remember { mutableStateOf(target.teachSkills.firstOrNull() ?: "Python") }
        var messageInput by remember { mutableStateOf("Hi! I'd love to set up study sessions this week.") }

        AlertDialog(
            onDismissRequest = { showRequestDialog = null },
            title = { Text("Exchange with ${target.userName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select what you will teach:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = teachSkillInput,
                        onValueChange = { teachSkillInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Skill you wish to learn from ${target.userName}:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = learnSkillInput,
                        onValueChange = { learnSkillInput = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text("Intro Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendRequest(target.userName, teachSkillInput, learnSkillInput, messageInput)
                        showRequestDialog = null
                        selectedTab = 1
                        Toast.makeText(context, "Request sent to ${target.userName}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
                ) {
                    Text("Send Proposal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
