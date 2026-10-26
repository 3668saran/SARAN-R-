package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrustCheckScreen(
    user: UserProfile,
    submissions: List<TrustCheckSubmission>,
    auditLogs: List<AuditLog>,
    onSubmitVerification: suspend (String, String, String) -> Unit,
    onReviewSubmission: (String, Boolean, String) -> Unit,
    onSwitchRole: (UserRole) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Portal, 1: Staff Review Queue, 2: Audit Trail

    // Student form state
    var regNumber by remember { mutableStateOf(user.collegeRegNumber.ifBlank { "REG-2024-CS-8921" }) }
    var documentType by remember { mutableStateOf("Semester Grade Marksheet") }
    var selectedFileName by remember { mutableStateOf("alex_official_marksheet_sem4.pdf") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showAiPreCheckModal by remember { mutableStateOf(false) }
    var aiPreCheckResult by remember { mutableStateOf<Pair<String, List<String>>?>(null) }

    // Staff review state
    var selectedSubmissionForReview by remember { mutableStateOf<TrustCheckSubmission?>(null) }
    var reviewRemarksInput by remember { mutableStateOf("") }
    var showReviewActionDialog by remember { mutableStateOf<Boolean?>(null) } // true: approve, false: reject

    val docTypes = listOf(
        "Semester Grade Marksheet",
        "Degree / Provisional Certificate",
        "Official Campus Transcript",
        "Institutional Photo ID"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trustcheck_screen")
    ) {
        // TrustCheck Title Banner
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = "TrustCheck",
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "TrustCheck Verification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Perspective Switcher for Testing
                AssistChip(
                    onClick = {
                        val nextRole = when (user.role) {
                            UserRole.STUDENT -> UserRole.STAFF
                            UserRole.STAFF -> UserRole.ADMIN
                            UserRole.ADMIN -> UserRole.STUDENT
                        }
                        onSwitchRole(nextRole)
                    },
                    label = {
                        Text(
                            text = "Role: ${user.role.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.SwitchAccount,
                            contentDescription = "Switch",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }

            Text(
                text = "Secure academic credential verification with Gemma AI pre-screening and authorized human staff review",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("My Verification", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Staff Queue (${submissions.count { it.status == VerificationStatus.PENDING }})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Audit Trail", fontWeight = FontWeight.Bold) }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // --- STUDENT VERIFICATION VIEW ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Status Alert Card
                    item {
                        val myLatest = submissions.find { it.studentName.contains("Alex") }
                        val currentStatus = myLatest?.status ?: user.status

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (currentStatus) {
                                    VerificationStatus.VERIFIED -> SuccessGreen.copy(alpha = 0.12f)
                                    VerificationStatus.PENDING -> WarningOrange.copy(alpha = 0.12f)
                                    VerificationStatus.REJECTED -> ErrorRed.copy(alpha = 0.12f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = when (currentStatus) {
                                            VerificationStatus.VERIFIED -> Icons.Filled.Verified
                                            VerificationStatus.PENDING -> Icons.Filled.HourglassTop
                                            VerificationStatus.REJECTED -> Icons.Filled.Warning
                                            else -> Icons.Outlined.Shield
                                        },
                                        contentDescription = "Status",
                                        tint = when (currentStatus) {
                                            VerificationStatus.VERIFIED -> SuccessGreen
                                            VerificationStatus.PENDING -> WarningOrange
                                            VerificationStatus.REJECTED -> ErrorRed
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = when (currentStatus) {
                                                VerificationStatus.VERIFIED -> "Account Status: TrustCheck Verified"
                                                VerificationStatus.PENDING -> "Account Status: Under Staff Review"
                                                VerificationStatus.REJECTED -> "Account Status: Action Required / Correction Needed"
                                                else -> "Account Status: Unverified Student"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = when (currentStatus) {
                                                VerificationStatus.VERIFIED -> "Your official academic record has been approved by the institution registry."
                                                VerificationStatus.PENDING -> "Uploaded document is in review queue. AI pre-check completed."
                                                VerificationStatus.REJECTED -> myLatest?.reviewRemarks ?: "Document could not be verified. Please review feedback and resubmit."
                                                else -> "Submit your college registration number and original marksheet to earn verified peer badges."
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (myLatest != null && currentStatus == VerificationStatus.VERIFIED) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SuccessGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "Endorsed by ${myLatest.reviewedBy} on ${myLatest.reviewDate}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SuccessGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Submission Form
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Submit Academic Record for Verification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )

                                OutlinedTextField(
                                    value = regNumber,
                                    onValueChange = { regNumber = it },
                                    label = { Text("College Registration / Enrollment #") },
                                    placeholder = { Text("e.g. REG-2024-CS-8921") },
                                    leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = PrimaryIndigo) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Text("Document Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                docTypes.forEach { type ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { documentType = type }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = documentType == type,
                                            onClick = { documentType = type }
                                        )
                                        Text(text = type, fontSize = 13.sp)
                                    }
                                }

                                // Upload / File Attachment Preview
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                                    shape = RoundedCornerShape(14.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.05f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.UploadFile,
                                            contentDescription = "Upload",
                                            tint = PrimaryIndigo,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = selectedFileName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "PDF or High-Res Image (Max 10MB) • Private Storage",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FilledTonalButton(
                                            onClick = {
                                                selectedFileName = "scanned_marksheet_sem4_signed.pdf"
                                                Toast.makeText(context, "Attached scanned document", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Change Selected Document", fontSize = 12.sp)
                                        }
                                    }
                                }

                                // AI Pre-Check Button
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val ai = AiAssistantService()
                                            aiPreCheckResult = ai.performTrustCheckAiPreCheck(documentType, regNumber)
                                            showAiPreCheckModal = true
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = PrimaryIndigo)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Run Gemma AI Pre-Check on Marksheet", fontWeight = FontWeight.Bold)
                                }

                                // Final Submit Button
                                Button(
                                    onClick = {
                                        isSubmitting = true
                                        scope.launch {
                                            onSubmitVerification(regNumber, documentType, selectedFileName)
                                            isSubmitting = false
                                            Toast.makeText(context, "Verification document submitted for review", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    enabled = !isSubmitting && regNumber.isNotBlank()
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                    } else {
                                        Text("Submit to Authorized Registry Staff", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Academic Integrity & Privacy Notice
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(18.dp))
                                    Text("Privacy & Zero-Knowledge Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Registration numbers and marksheets are never exposed in public forums or leaderboards. Documents are stored in encrypted private storage and only accessible to authorized institutional staff reviewers.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // --- STAFF VERIFICATION DASHBOARD ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(28.dp))
                                Column {
                                    Text("Authorized Registry Review Portal", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Authorized Staff: ${user.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    items(submissions) { sub ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("staff_sub_${sub.id}"),
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
                                    Column {
                                        Text(text = sub.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "Reg No: ${sub.collegeRegNo}", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (sub.status) {
                                            VerificationStatus.VERIFIED -> SuccessGreen.copy(alpha = 0.15f)
                                            VerificationStatus.PENDING -> WarningOrange.copy(alpha = 0.15f)
                                            VerificationStatus.REJECTED -> ErrorRed.copy(alpha = 0.15f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ) {
                                        Text(
                                            text = sub.status.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (sub.status) {
                                                VerificationStatus.VERIFIED -> SuccessGreen
                                                VerificationStatus.PENDING -> WarningOrange
                                                VerificationStatus.REJECTED -> ErrorRed
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(text = "Document: ${sub.documentType} (${sub.documentName})", fontSize = 12.sp)
                                Text(text = "Institution: ${sub.institution}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (sub.aiExtractedDetails.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Gemma AI Extraction Note:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = PrimaryIndigo)
                                            Text(text = sub.aiExtractedDetails, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                if (sub.reviewRemarks.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Staff Remarks: ${sub.reviewRemarks}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (sub.status == VerificationStatus.VERIFIED) SuccessGreen else ErrorRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Staff Review Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            selectedSubmissionForReview = sub
                                            showReviewActionDialog = true
                                            reviewRemarksInput = "Verified with institutional registrar records. Signature confirmed."
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedSubmissionForReview = sub
                                            showReviewActionDialog = false
                                            reviewRemarksInput = "The submitted scan is blurry. Please upload a clear original copy with the official seal."
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject / Edit")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // --- AUDIT TRAIL VIEW ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Immutable Compliance & Moderation Log",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PrimaryIndigo
                                    )
                                    Text(
                                        text = log.timestamp,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "By ${log.operatorName} (${log.operatorRole.name}) • Target: ${log.targetRecord}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = log.details,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Pre-Check Dialog Modal
    if (showAiPreCheckModal && aiPreCheckResult != null) {
        val (details, flags) = aiPreCheckResult!!
        AlertDialog(
            onDismissRequest = { showAiPreCheckModal = false },
            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = PrimaryIndigo) },
            title = { Text("Gemma AI Document Pre-Screening") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "The AI assists with field extraction and formatting consistency checks:",
                        fontSize = 12.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = details,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    if (flags.isNotEmpty()) {
                        Text("Advisories & Integrity Checks:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        flags.forEach { f ->
                            Text("• $f", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Text(
                        text = "Notice: AI pre-check does NOT independently certify authenticity. Verification must be approved by an authorized human reviewer.",
                        fontSize = 10.sp,
                        color = WarningOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAiPreCheckModal = false }) {
                    Text("Close Pre-Check")
                }
            }
        )
    }

    // Staff Review Action Dialog (Approve or Reject with reason)
    if (showReviewActionDialog != null && selectedSubmissionForReview != null) {
        val isApprove = showReviewActionDialog!!
        val sub = selectedSubmissionForReview!!

        AlertDialog(
            onDismissRequest = {
                showReviewActionDialog = null
                selectedSubmissionForReview = null
            },
            title = { Text(if (isApprove) "Approve Verification for ${sub.studentName}" else "Request Correction / Reject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isApprove) "Record official approval and registrar endorsement:" else "Provide mandatory reason for student feedback:",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = reviewRemarksInput,
                        onValueChange = { reviewRemarksInput = it },
                        label = { Text(if (isApprove) "Endorsement Notes" else "Rejection / Correction Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReviewSubmission(sub.id, isApprove, reviewRemarksInput)
                        showReviewActionDialog = null
                        selectedSubmissionForReview = null
                        Toast.makeText(context, if (isApprove) "Submission approved!" else "Correction requested", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApprove) SuccessGreen else ErrorRed
                    ),
                    enabled = reviewRemarksInput.isNotBlank()
                ) {
                    Text(if (isApprove) "Confirm Approval" else "Send Correction Request")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showReviewActionDialog = null
                    selectedSubmissionForReview = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
