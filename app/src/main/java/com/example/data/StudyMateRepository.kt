package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StudyMateRepository(
    private val aiService: AiAssistantService = AiAssistantService()
) {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user-std-101",
            name = "Alex Morgan",
            email = "alex.morgan@university.edu",
            role = UserRole.STUDENT,
            collegeRegNumber = "REG-2024-CS-8921",
            collegeName = "Institute of Technology & Science",
            status = VerificationStatus.VERIFIED,
            points = 340,
            avatarColorHex = 0xFF3B48DF
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "chat-1",
                prompt = "Explain Dijkstra's algorithm simply with time complexity",
                response = """
                ### Dijkstra's Shortest Path Algorithm
                **Concept:** Finds the minimum distance from a single starting vertex to all other vertices in a weighted graph with positive weights.
                
                **Key Steps:**
                1. Initialize distances: Start node = 0, all others = ∞.
                2. Use a Priority Queue (min-heap) to greedily pick the closest unvisited node.
                3. Relax adjacent edges: if `dist[u] + weight(u, v) < dist[v]`, update `dist[v]`.
                4. Mark node as processed.
                
                **Complexity:** `O((V + E) log V)` using Min-Heap.
                """.trimIndent(),
                timestamp = "10:14 AM",
                isSaved = true,
                subjectTag = "Algorithms"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _communityPosts = MutableStateFlow<List<CommunityPost>>(
        listOf(
            CommunityPost(
                id = "post-1",
                authorName = "Sophia Chen",
                authorRole = UserRole.STUDENT,
                authorVerified = true,
                title = "Best resources for mastering Operating System concurrency?",
                content = "Preparing for midterm exams. Looking for visual animations or intuitive guides on Semaphores vs Mutexes, and avoiding Deadlocks.",
                subject = "Computer Science",
                upvotes = 24,
                hasUpvoted = false,
                commentsCount = 8,
                timestamp = "2 hours ago"
            ),
            CommunityPost(
                id = "post-2",
                authorName = "Prof. Daniel Vance",
                authorRole = UserRole.STAFF,
                authorVerified = true,
                title = "Academic Guidelines: Citing AI tools in Research Papers",
                content = "A reminder to all students submitting term projects: when using LLMs for code scaffolding or literature synthesis, cite the tool and document your prompts in the appendix.",
                subject = "Academic Policy",
                upvotes = 56,
                hasUpvoted = true,
                commentsCount = 14,
                timestamp = "Yesterday"
            ),
            CommunityPost(
                id = "post-3",
                authorName = "Marcus Brody",
                authorRole = UserRole.STUDENT,
                authorVerified = false,
                title = "Calculus III: Vector fields and Green's Theorem shortcut tips",
                content = "Compiled a cheat-sheet of line integrals around closed curves. Feel free to comment if you have questions on parameterization!",
                subject = "Mathematics",
                upvotes = 31,
                hasUpvoted = false,
                commentsCount = 5,
                timestamp = "3 days ago"
            )
        )
    )
    val communityPosts: StateFlow<List<CommunityPost>> = _communityPosts.asStateFlow()

    private val _skillListings = MutableStateFlow<List<SkillListing>>(
        listOf(
            SkillListing(
                id = "skill-1",
                userId = "u-201",
                userName = "Elena Rostova",
                collegeName = "Tech Institute",
                teachSkills = listOf("Python", "FastAPI", "Pandas"),
                learnSkills = listOf("React", "Tailwind CSS"),
                rating = 4.9,
                completedSwaps = 12
            ),
            SkillListing(
                id = "skill-2",
                userId = "u-202",
                userName = "Karan Patel",
                collegeName = "Engineering Campus",
                teachSkills = listOf("Linear Algebra", "Calculus II"),
                learnSkills = listOf("Machine Learning", "PyTorch"),
                rating = 4.8,
                completedSwaps = 7
            ),
            SkillListing(
                id = "skill-3",
                userId = "u-203",
                userName = "Jessica Taylor",
                collegeName = "Arts & Sciences",
                teachSkills = listOf("Academic Writing", "IELTS Prep"),
                learnSkills = listOf("Public Speaking", "French"),
                rating = 5.0,
                completedSwaps = 19
            )
        )
    )
    val skillListings: StateFlow<List<SkillListing>> = _skillListings.asStateFlow()

    private val _skillRequests = MutableStateFlow<List<SkillExchangeRequest>>(
        listOf(
            SkillExchangeRequest(
                id = "req-1",
                fromUserName = "Elena Rostova",
                toUserName = "Alex Morgan",
                teachOffered = "Python Data Analysis",
                learnRequested = "Kotlin & Jetpack Compose",
                message = "Hi Alex! Saw your profile. Would love to trade 2 hours of Python for intro to Compose navigation.",
                status = ExchangeStatus.PENDING,
                date = "Today"
            ),
            SkillExchangeRequest(
                id = "req-2",
                fromUserName = "Alex Morgan",
                toUserName = "Karan Patel",
                teachOffered = "Database Schema Design",
                learnRequested = "Linear Algebra Transformations",
                message = "Let's connect for weekend study sessions.",
                status = ExchangeStatus.ACCEPTED,
                date = "Oct 24"
            )
        )
    )
    val skillRequests: StateFlow<List<SkillExchangeRequest>> = _skillRequests.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(
        listOf(
            Quiz(
                id = "quiz-py-1",
                title = "Python Core & Data Structures",
                subject = "Computer Science",
                difficulty = "Intermediate",
                estimatedTimeMinutes = 5,
                questions = listOf(
                    QuizQuestion(
                        question = "Which of the following data structures in Python is immutable?",
                        options = listOf("List", "Dictionary", "Tuple", "Set"),
                        correctIndex = 2,
                        explanation = "Tuples are immutable in Python; once initialized, their elements cannot be modified, added, or removed."
                    ),
                    QuizQuestion(
                        question = "What is the average time complexity for searching a key in a Python dictionary?",
                        options = listOf("O(1)", "O(log N)", "O(N)", "O(N²)"),
                        correctIndex = 0,
                        explanation = "Python dictionaries are implemented using hash tables, giving average O(1) constant time lookups."
                    ),
                    QuizQuestion(
                        question = "What is the output of bool([]) in Python?",
                        options = listOf("True", "False", "None", "TypeError"),
                        correctIndex = 1,
                        explanation = "Empty collections in Python evaluate to False in a boolean context."
                    )
                )
            ),
            Quiz(
                id = "quiz-algo-1",
                title = "Algorithms & Complexity Essentials",
                subject = "Computer Science",
                difficulty = "Advanced",
                estimatedTimeMinutes = 6,
                questions = listOf(
                    QuizQuestion(
                        question = "Which sorting algorithm achieves worst-case O(N log N) time complexity?",
                        options = listOf("Quick Sort", "Merge Sort", "Bubble Sort", "Insertion Sort"),
                        correctIndex = 1,
                        explanation = "Merge Sort always divides arrays evenly and merges in O(N), guaranteeing O(N log N) even in the worst case."
                    ),
                    QuizQuestion(
                        question = "In a binary search tree, what traversal visits nodes in ascending sorted order?",
                        options = listOf("Pre-order", "In-order", "Post-order", "Level-order"),
                        correctIndex = 1,
                        explanation = "In-order traversal visits left subtree, root, then right subtree, visiting keys in strictly ascending order."
                    )
                )
            ),
            Quiz(
                id = "quiz-math-1",
                title = "Discrete Mathematics & Logic",
                subject = "Mathematics",
                difficulty = "Beginner",
                estimatedTimeMinutes = 4,
                questions = listOf(
                    QuizQuestion(
                        question = "If proposition P is True and Q is False, what is the truth value of P → Q (conditional)?",
                        options = listOf("True", "False", "Indeterminate", "Paradoxical"),
                        correctIndex = 1,
                        explanation = "A conditional implication P → Q is False if and only if the antecedent P is True and consequent Q is False."
                    )
                )
            )
        )
    )
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _recentAttempts = MutableStateFlow<List<QuizAttempt>>(
        listOf(
            QuizAttempt(
                id = "att-1",
                quizTitle = "Python Core & Data Structures",
                score = 3,
                total = 3,
                date = "Oct 25, 2026",
                isChallenge = false
            ),
            QuizAttempt(
                id = "att-2",
                quizTitle = "Algorithms & Complexity Essentials",
                score = 2,
                total = 2,
                date = "Oct 23, 2026",
                isChallenge = true
            )
        )
    )
    val recentAttempts: StateFlow<List<QuizAttempt>> = _recentAttempts.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(
        listOf(
            LeaderboardEntry(rank = 1, name = "Samantha Miller", points = 580, quizzesCompleted = 22, verified = true),
            LeaderboardEntry(rank = 2, name = "Alex Morgan", points = 340, quizzesCompleted = 14, verified = true),
            LeaderboardEntry(rank = 3, name = "David Zhang", points = 310, quizzesCompleted = 11, verified = true),
            LeaderboardEntry(rank = 4, name = "Maya Patel", points = 285, quizzesCompleted = 10, verified = false),
            LeaderboardEntry(rank = 5, name = "Chloe Kim", points = 240, quizzesCompleted = 8, verified = true)
        )
    )
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _studyTasks = MutableStateFlow<List<StudyTask>>(
        listOf(
            StudyTask(
                id = "task-1",
                title = "Complete Operating Systems Lab Assignment 4",
                subject = "Computer Science",
                priority = TaskPriority.HIGH,
                dueDate = "Tomorrow, 5:00 PM",
                isCompleted = false
            ),
            StudyTask(
                id = "task-2",
                title = "Review Discrete Math Chapter 5 (Equivalence Relations)",
                subject = "Mathematics",
                priority = TaskPriority.MEDIUM,
                dueDate = "Oct 28, 2026",
                isCompleted = false
            ),
            StudyTask(
                id = "task-3",
                title = "Submit TrustCheck semester transcript document",
                subject = "Verification",
                priority = TaskPriority.HIGH,
                dueDate = "Done",
                isCompleted = true
            ),
            StudyTask(
                id = "task-4",
                title = "Prepare SkillSwap session notes with Elena",
                subject = "Peer Learning",
                priority = TaskPriority.LOW,
                dueDate = "Oct 30, 2026",
                isCompleted = false
            )
        )
    )
    val studyTasks: StateFlow<List<StudyTask>> = _studyTasks.asStateFlow()

    private val _trustCheckSubmissions = MutableStateFlow<List<TrustCheckSubmission>>(
        listOf(
            TrustCheckSubmission(
                id = "sub-8921",
                studentName = "Alex Morgan",
                studentEmail = "alex.morgan@university.edu",
                collegeRegNo = "REG-2024-CS-8921",
                institution = "Institute of Technology & Science",
                documentType = "Semester Grade Transcript",
                documentName = "alex_morgan_sem4_marksheet.pdf",
                uploadDate = "Oct 24, 2026",
                status = VerificationStatus.VERIFIED,
                aiExtractedDetails = "Extracted: Alex Morgan, REG-2024-CS-8921, CGPA: 3.86/4.0, Registrar Seal: Verified",
                aiFlags = emptyList(),
                reviewRemarks = "Official registrar seal and digital watermark match institutional database.",
                reviewedBy = "Dr. Miller (Academic Registry)",
                reviewDate = "Oct 25, 2026"
            ),
            TrustCheckSubmission(
                id = "sub-7742",
                studentName = "Liam Gallagher",
                studentEmail = "liam.g@university.edu",
                collegeRegNo = "REG-2024-ME-7742",
                institution = "College of Mechanical Sciences",
                documentType = "Provisional Marksheet",
                documentName = "liam_marksheet_scanned.jpg",
                uploadDate = "Oct 26, 2026",
                status = VerificationStatus.PENDING,
                aiExtractedDetails = "Extracted: Liam Gallagher, REG-2024-ME-7742, CGPA: 3.42/4.0. Seal clarity: Moderate.",
                aiFlags = listOf("Seal image slightly rotated", "Pending registrar signature verification"),
                reviewRemarks = "",
                reviewedBy = "",
                reviewDate = ""
            )
        )
    )
    val trustCheckSubmissions: StateFlow<List<TrustCheckSubmission>> = _trustCheckSubmissions.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(
        listOf(
            AuditLog(
                id = "aud-1",
                action = "VERIFICATION_APPROVED",
                operatorName = "Dr. Miller",
                operatorRole = UserRole.STAFF,
                targetRecord = "Submission #sub-8921 (Alex Morgan)",
                timestamp = "Oct 25, 2026 14:32",
                details = "Approved academic marksheet after matching institutional registrar seal."
            ),
            AuditLog(
                id = "aud-2",
                action = "COMMUNITY_POST_CREATED",
                operatorName = "Sophia Chen",
                operatorRole = UserRole.STUDENT,
                targetRecord = "Post #post-1",
                timestamp = "Oct 26, 2026 09:15",
                details = "Created discussion in Computer Science category."
            )
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    // --- Actions ---

    fun switchUserRole(role: UserRole) {
        val current = _currentUser.value
        _currentUser.value = current.copy(
            role = role,
            name = when (role) {
                UserRole.STUDENT -> "Alex Morgan (Student)"
                UserRole.STAFF -> "Dr. Evelyn Vance (Authorized Reviewer)"
                UserRole.ADMIN -> "Dean Robert Hayes (Platform Admin)"
            }
        )
        recordAudit(
            action = "ROLE_SWITCHED",
            operatorName = _currentUser.value.name,
            role = role,
            target = "User ${_currentUser.value.id}",
            detail = "Switched testing perspective to ${role.name}"
        )
    }

    suspend fun askAssistant(prompt: String, subject: String): ChatMessage {
        val response = aiService.askGemma(prompt, subject)
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            prompt = prompt,
            response = response,
            timestamp = timeFormat.format(Date()),
            isSaved = false,
            subjectTag = subject
        )
        _chatMessages.value = listOf(msg) + _chatMessages.value
        return msg
    }

    fun toggleSaveChat(chatId: String) {
        _chatMessages.value = _chatMessages.value.map {
            if (it.id == chatId) it.copy(isSaved = !it.isSaved) else it
        }
    }

    fun toggleTask(taskId: String) {
        _studyTasks.value = _studyTasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun addTask(title: String, subject: String, priority: TaskPriority, dueDate: String) {
        val task = StudyTask(
            id = UUID.randomUUID().toString(),
            title = title,
            subject = subject.ifBlank { "General" },
            priority = priority,
            dueDate = dueDate.ifBlank { "Tomorrow" },
            isCompleted = false
        )
        _studyTasks.value = listOf(task) + _studyTasks.value
    }

    fun deleteTask(taskId: String) {
        _studyTasks.value = _studyTasks.value.filter { it.id != taskId }
    }

    fun upvotePost(postId: String) {
        _communityPosts.value = _communityPosts.value.map {
            if (it.id == postId) {
                val newHasUpvoted = !it.hasUpvoted
                it.copy(
                    hasUpvoted = newHasUpvoted,
                    upvotes = if (newHasUpvoted) it.upvotes + 1 else it.upvotes - 1
                )
            } else it
        }
    }

    fun createPost(title: String, content: String, subject: String) {
        val user = _currentUser.value
        val post = CommunityPost(
            id = UUID.randomUUID().toString(),
            authorName = user.name,
            authorRole = user.role,
            authorVerified = user.status == VerificationStatus.VERIFIED,
            title = title,
            content = content,
            subject = subject.ifBlank { "General" },
            upvotes = 1,
            hasUpvoted = true,
            commentsCount = 0,
            timestamp = "Just now"
        )
        _communityPosts.value = listOf(post) + _communityPosts.value
        recordAudit("POST_CREATED", user.name, user.role, post.id, "Published new post in $subject")
    }

    fun reportPost(postId: String) {
        _communityPosts.value = _communityPosts.value.map {
            if (it.id == postId) it.copy(isReported = true) else it
        }
        recordAudit("POST_REPORTED", _currentUser.value.name, _currentUser.value.role, postId, "Reported for community guideline review")
    }

    fun sendSkillRequest(toUserName: String, teachOffered: String, learnRequested: String, message: String) {
        val req = SkillExchangeRequest(
            id = UUID.randomUUID().toString(),
            fromUserName = _currentUser.value.name,
            toUserName = toUserName,
            teachOffered = teachOffered,
            learnRequested = learnRequested,
            message = message,
            status = ExchangeStatus.PENDING,
            date = "Today"
        )
        _skillRequests.value = listOf(req) + _skillRequests.value
    }

    fun updateSkillRequestStatus(requestId: String, status: ExchangeStatus) {
        _skillRequests.value = _skillRequests.value.map {
            if (it.id == requestId) it.copy(status = status) else it
        }
    }

    fun submitQuizScore(quizTitle: String, score: Int, total: Int, isChallenge: Boolean = false) {
        val attempt = QuizAttempt(
            id = UUID.randomUUID().toString(),
            quizTitle = quizTitle,
            score = score,
            total = total,
            date = dateFormat.format(Date()),
            isChallenge = isChallenge
        )
        _recentAttempts.value = listOf(attempt) + _recentAttempts.value
        // award points
        _currentUser.value = _currentUser.value.copy(
            points = _currentUser.value.points + (score * 10)
        )
    }

    suspend fun submitTrustCheck(
        regNo: String,
        docType: String,
        fileName: String
    ): TrustCheckSubmission {
        val user = _currentUser.value
        val (aiDetails, aiFlags) = aiService.performTrustCheckAiPreCheck(docType, regNo)

        val sub = TrustCheckSubmission(
            id = "sub-" + (1000..9999).random(),
            studentName = user.name,
            studentEmail = user.email,
            collegeRegNo = regNo.ifBlank { user.collegeRegNumber },
            institution = user.collegeName.ifBlank { "National Institute of Science" },
            documentType = docType,
            documentName = fileName.ifBlank { "marksheet_official.pdf" },
            uploadDate = dateFormat.format(Date()),
            status = VerificationStatus.PENDING,
            aiExtractedDetails = aiDetails,
            aiFlags = aiFlags,
            reviewRemarks = "Awaiting verification by authorized registry staff.",
            reviewedBy = "",
            reviewDate = ""
        )

        _trustCheckSubmissions.value = listOf(sub) + _trustCheckSubmissions.value
        recordAudit("TRUSTCHECK_SUBMITTED", user.name, user.role, sub.id, "Uploaded $docType with Reg: $regNo")
        return sub
    }

    fun reviewTrustCheck(submissionId: String, approve: Boolean, reason: String) {
        val staff = _currentUser.value
        _trustCheckSubmissions.value = _trustCheckSubmissions.value.map { sub ->
            if (sub.id == submissionId) {
                sub.copy(
                    status = if (approve) VerificationStatus.VERIFIED else VerificationStatus.REJECTED,
                    reviewRemarks = reason,
                    reviewedBy = staff.name,
                    reviewDate = dateFormat.format(Date())
                )
            } else sub
        }

        // If the reviewed submission belongs to current user, update user status as well
        val reviewedSub = _trustCheckSubmissions.value.find { it.id == submissionId }
        if (reviewedSub != null && reviewedSub.studentName.contains("Alex")) {
            _currentUser.value = _currentUser.value.copy(
                status = if (approve) VerificationStatus.VERIFIED else VerificationStatus.REJECTED
            )
        }

        recordAudit(
            action = if (approve) "VERIFICATION_APPROVED" else "VERIFICATION_REJECTED",
            operatorName = staff.name,
            role = staff.role,
            target = "Submission #$submissionId",
            detail = "Outcome: ${if (approve) "Verified" else "Needs Correction"}. Remarks: $reason"
        )
    }

    private fun recordAudit(action: String, operatorName: String, role: UserRole, target: String, detail: String) {
        val log = AuditLog(
            id = "aud-" + UUID.randomUUID().toString().take(6),
            action = action,
            operatorName = operatorName,
            operatorRole = role,
            targetRecord = target,
            timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date()),
            details = detail
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
