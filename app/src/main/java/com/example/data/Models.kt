package com.example.data

enum class UserRole {
    STUDENT,
    STAFF,
    ADMIN
}

enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}

enum class TaskPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class ExchangeStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole = UserRole.STUDENT,
    val collegeRegNumber: String = "",
    val collegeName: String = "",
    val status: VerificationStatus = VerificationStatus.UNVERIFIED,
    val points: Int = 120,
    val avatarColorHex: Long = 0xFF3B48DF
)

data class ChatMessage(
    val id: String,
    val prompt: String,
    val response: String,
    val timestamp: String,
    val isSaved: Boolean = false,
    val subjectTag: String = "General",
    val isExplanation: Boolean = true
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorRole: UserRole,
    val authorVerified: Boolean,
    val title: String,
    val content: String,
    val subject: String,
    val upvotes: Int,
    val hasUpvoted: Boolean = false,
    val commentsCount: Int = 0,
    val timestamp: String,
    val isReported: Boolean = false
)

data class CommunityComment(
    val id: String,
    val postId: String,
    val authorName: String,
    val content: String,
    val timestamp: String
)

data class SkillListing(
    val id: String,
    val userId: String,
    val userName: String,
    val collegeName: String,
    val teachSkills: List<String>,
    val learnSkills: List<String>,
    val rating: Double,
    val completedSwaps: Int,
    val isAvailable: Boolean = true
)

data class SkillExchangeRequest(
    val id: String,
    val fromUserName: String,
    val toUserName: String,
    val teachOffered: String,
    val learnRequested: String,
    val message: String,
    val status: ExchangeStatus,
    val date: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class Quiz(
    val id: String,
    val title: String,
    val subject: String,
    val difficulty: String,
    val estimatedTimeMinutes: Int,
    val questions: List<QuizQuestion>
)

data class QuizAttempt(
    val id: String,
    val quizTitle: String,
    val score: Int,
    val total: Int,
    val date: String,
    val isChallenge: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val quizzesCompleted: Int,
    val verified: Boolean
)

data class StudyTask(
    val id: String,
    val title: String,
    val subject: String,
    val priority: TaskPriority,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val reminderSet: Boolean = true
)

data class TrustCheckSubmission(
    val id: String,
    val studentName: String,
    val studentEmail: String,
    val collegeRegNo: String,
    val institution: String,
    val documentType: String,
    val documentName: String,
    val uploadDate: String,
    val status: VerificationStatus,
    val aiExtractedDetails: String,
    val aiFlags: List<String> = emptyList(),
    val reviewRemarks: String = "",
    val reviewedBy: String = "",
    val reviewDate: String = ""
)

data class AuditLog(
    val id: String,
    val action: String,
    val operatorName: String,
    val operatorRole: UserRole,
    val targetRecord: String,
    val timestamp: String,
    val details: String
)
