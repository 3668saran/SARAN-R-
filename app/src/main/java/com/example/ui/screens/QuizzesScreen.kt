package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun QuizzesScreen(
    quizzes: List<Quiz>,
    attempts: List<QuizAttempt>,
    leaderboard: List<LeaderboardEntry>,
    onSubmitScore: (String, Int, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Quizzes, 1: Leaderboard, 2: History
    var activeQuiz by remember { mutableStateOf<Quiz?>(null) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var hasSubmittedAnswer by remember { mutableStateOf(false) }
    var runningScore by remember { mutableIntStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    // If taking a quiz
    if (activeQuiz != null) {
        val quiz = activeQuiz!!
        val question = quiz.questions[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("quiz_runner_view")
        ) {
            // Header with Back & Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { activeQuiz = null }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Quiz")
                }
                Text(
                    text = "${quiz.title}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "${currentQuestionIndex + 1}/${quiz.questions.size}",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
            }

            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1).toFloat() / quiz.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = PrimaryIndigo,
                trackColor = PrimaryIndigo.copy(alpha = 0.2f)
            )

            if (!isQuizCompleted) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = question.question,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    items(question.options.indices.toList()) { idx ->
                        val optText = question.options[idx]
                        val isSelected = selectedOptionIndex == idx
                        val isCorrect = question.correctIndex == idx

                        val cardColor = when {
                            hasSubmittedAnswer && isCorrect -> SuccessGreen.copy(alpha = 0.2f)
                            hasSubmittedAnswer && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.2f)
                            isSelected -> PrimaryIndigo.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            hasSubmittedAnswer && isCorrect -> SuccessGreen
                            hasSubmittedAnswer && isSelected && !isCorrect -> ErrorRed
                            isSelected -> PrimaryIndigo
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !hasSubmittedAnswer) {
                                    selectedOptionIndex = idx
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(borderColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${('A' + idx)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (hasSubmittedAnswer && isCorrect) SuccessGreen else PrimaryIndigo
                                    )
                                }
                                Text(
                                    text = optText,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasSubmittedAnswer && isCorrect) {
                                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "Correct", tint = SuccessGreen)
                                } else if (hasSubmittedAnswer && isSelected && !isCorrect) {
                                    Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Incorrect", tint = ErrorRed)
                                }
                            }
                        }
                    }

                    // Explanation Card after submission
                    if (hasSubmittedAnswer) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedOptionIndex == question.correctIndex)
                                        SuccessGreen.copy(alpha = 0.1f)
                                    else
                                        WarningOrange.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (selectedOptionIndex == question.correctIndex) "✓ Correct Answer!" else "✕ Explanation:",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedOptionIndex == question.correctIndex) SuccessGreen else WarningOrange,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = question.explanation,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Action Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp
                ) {
                    if (!hasSubmittedAnswer) {
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null) {
                                    hasSubmittedAnswer = true
                                    if (selectedOptionIndex == question.correctIndex) {
                                        runningScore += 1
                                    }
                                }
                            },
                            enabled = selectedOptionIndex != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            Text("Submit Answer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentQuestionIndex < quiz.questions.size - 1) {
                                    currentQuestionIndex += 1
                                    selectedOptionIndex = null
                                    hasSubmittedAnswer = false
                                } else {
                                    isQuizCompleted = true
                                    onSubmitScore(quiz.title, runningScore, quiz.questions.size, false)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                        ) {
                            Text(
                                text = if (currentQuestionIndex < quiz.questions.size - 1) "Next Question" else "Complete Quiz & View Score",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Completed Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AccentAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Complete",
                            tint = AccentAmber,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Quiz Completed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "You scored $runningScore out of ${quiz.questions.size}",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "+${runningScore * 10}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryIndigo)
                                Text(text = "Rep Points", fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${((runningScore.toFloat() / quiz.questions.size) * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = SuccessGreen
                                )
                                Text(text = "Accuracy", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            activeQuiz = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Back to Quizzes")
                    }
                }
            }
        }
        return
    }

    // Default Main Quiz View
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quizzes_screen")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Quizzes & Academic Challenges",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Test your grasp with AI-curated practice quizzes and peer challenges",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Practice", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Leaderboard", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("My History", fontWeight = FontWeight.Bold) })
            }
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Friend Challenge Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(32.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Friend Challenge Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Challenge a study group peer to beat your speed & score", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilledTonalButton(
                                onClick = {
                                    Toast.makeText(context, "Challenge link copied: studymate.ai/c/cs-algo-49", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Invite")
                            }
                        }
                    }
                }

                items(quizzes) { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_card_${quiz.id}"),
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
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = quiz.subject,
                                        color = PrimaryIndigo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Text(
                                    text = "${quiz.estimatedTimeMinutes} mins • ${quiz.questions.size} Questions",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = quiz.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (quiz.difficulty) {
                                        "Beginner" -> SuccessGreen.copy(alpha = 0.15f)
                                        "Intermediate" -> PrimaryIndigo.copy(alpha = 0.15f)
                                        else -> AccentRose.copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = quiz.difficulty,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (quiz.difficulty) {
                                            "Beginner" -> SuccessGreen
                                            "Intermediate" -> PrimaryIndigo
                                            else -> AccentRose
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        activeQuiz = quiz
                                        currentQuestionIndex = 0
                                        selectedOptionIndex = null
                                        hasSubmittedAnswer = false
                                        runningScore = 0
                                        isQuizCompleted = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Text("Start Quiz", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Leaderboard Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(leaderboard) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (entry.rank == 1) AccentAmber.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "#${entry.rank}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (entry.rank <= 3) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = entry.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (entry.verified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(text = "${entry.quizzesCompleted} quizzes completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text(
                                text = "${entry.points} pts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryIndigo
                            )
                        }
                    }
                }
            }
        } else {
            // History Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(attempts) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = att.quizTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Taken on ${att.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${att.score}/${att.total} Correct",
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
