package com.example

import com.example.data.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StudyMateRepositoryTest {

    private lateinit var repository: StudyMateRepository

    @Before
    fun setUp() {
        repository = StudyMateRepository()
    }

    @Test
    fun testInitialState() {
        val user = repository.currentUser.value
        assertEquals("Alex Morgan", user.name)
        assertEquals(UserRole.STUDENT, user.role)
        assertTrue(repository.studyTasks.value.isNotEmpty())
        assertTrue(repository.communityPosts.value.isNotEmpty())
        assertTrue(repository.quizzes.value.isNotEmpty())
    }

    @Test
    fun testTaskManagement() {
        val initialCount = repository.studyTasks.value.size
        repository.addTask("Solve Calculus Integrals", "Mathematics", TaskPriority.HIGH, "Tomorrow")
        assertEquals(initialCount + 1, repository.studyTasks.value.size)

        val newTask = repository.studyTasks.value.first()
        assertFalse(newTask.isCompleted)

        repository.toggleTask(newTask.id)
        val updatedTask = repository.studyTasks.value.first { it.id == newTask.id }
        assertTrue(updatedTask.isCompleted)
    }

    @Test
    fun testCommunityUpvote() {
        val post = repository.communityPosts.value.first()
        val initialUpvotes = post.upvotes

        repository.upvotePost(post.id)
        val upvoted = repository.communityPosts.value.first { it.id == post.id }
        assertEquals(initialUpvotes + 1, upvoted.upvotes)
        assertTrue(upvoted.hasUpvoted)

        // Toggle upvote back
        repository.upvotePost(post.id)
        val reverted = repository.communityPosts.value.first { it.id == post.id }
        assertEquals(initialUpvotes, reverted.upvotes)
        assertFalse(reverted.hasUpvoted)
    }

    @Test
    fun testTrustCheckSubmissionAndStaffReview() = runTest {
        val sub = repository.submitTrustCheck(
            regNo = "REG-2024-CS-9944",
            docType = "Degree Certificate",
            fileName = "degree_scan.pdf"
        )
        assertEquals(VerificationStatus.PENDING, sub.status)
        assertTrue(sub.aiExtractedDetails.isNotBlank())

        // Staff review approve
        repository.switchUserRole(UserRole.STAFF)
        repository.reviewTrustCheck(sub.id, approve = true, reason = "Verified against institutional registry.")

        val reviewed = repository.trustCheckSubmissions.value.first { it.id == sub.id }
        assertEquals(VerificationStatus.VERIFIED, reviewed.status)
        assertEquals("Verified against institutional registry.", reviewed.reviewRemarks)
    }
}
