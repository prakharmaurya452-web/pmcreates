package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Student",
    val grade: String = "Class 10",
    val curriculum: String = "CBSE",
    val academicSession: String = "2025-2026",
    val availableMinutesPerDay: Int = 150, // 2h 30m default
    val preferredStudyPeriod: String = "Evening", // Morning, Afternoon, Evening, Night
    val academicGoal: String = "Score 95%+ in Board Examinations",
    val isOnboarded: Boolean = false,
    val themeMode: String = "System", // System, Light, Dark
    val currentStreak: Int = 3,
    val totalXp: Int = 450,
    val level: Int = 2
)

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val grade: String = "Class 10",
    val targetHoursPerWeek: Int = 6,
    val orderIndex: Int = 0
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val orderIndex: Int = 0
)

@Entity(tableName = "topics")
data class Topic(
    @PrimaryKey val id: String,
    val chapterId: String,
    val subjectId: String,
    val title: String,
    val status: String = "Not Started", // Not Started, Learning, Practicing, Revised, Mastered
    val completionPercent: Int = 0,
    val lastStudiedTimestamp: Long = 0L,
    val lastRevisedTimestamp: Long = 0L,
    val accuracyPercent: Int = 75,
    val confidenceLevel: Int = 3, // 1 to 5
    val priority: String = "Medium", // Low, Medium, High, Urgent
    val estimatedMinutes: Int = 25,
    val notes: String = ""
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey val id: String,
    val name: String,
    val subjectNames: String, // Comma-separated or single subject
    val examDateTimestamp: Long, // Epoch ms
    val targetScore: Int = 95,
    val preparationStatus: String = "In Progress"
)

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey val id: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val activityType: String = "Practice", // Concept, Practice, Revision, Mistake Review, Assignment
    val durationMinutes: Int = 25,
    val priority: String = "High", // Low, Medium, High, Urgent
    val plannedDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = false,
    val reason: String = ""
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey val id: String,
    val subjectName: String,
    val chapterName: String = "",
    val topicTitle: String = "",
    val plannedMinutes: Int,
    val actualMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val focusRating: Int = 4, // 1-5
    val notes: String = "",
    val mode: String = "Pomodoro" // Pomodoro, Custom
)

@Entity(tableName = "mistakes")
data class Mistake(
    @PrimaryKey val id: String,
    val question: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mistakeType: String = "Concept", // Concept, Calculation, Memory, Reading, Careless, Time Management
    val status: String = "Unresolved" // Unresolved, Reviewing, Resolved
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String,
    val title: String,
    val target: String,
    val deadline: String,
    val progress: Int = 0, // 0 to 100
    val priority: String = "High",
    val status: String = "Active", // Active, Completed
    val category: String = "Weekly" // Daily, Weekly, Exam, Long-term
)

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val dueDateTimestamp: Long,
    val estimatedMinutes: Int = 40,
    val priority: String = "High",
    val status: String = "Pending", // Pending, In Progress, Completed
    val notes: String = ""
)

@Entity(tableName = "resources")
data class AcademicResource(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val resourceType: String, // OFFICIAL_CBSE, PYQ, SAMPLE_PAPER, COMPETENCY, UDAAN_LECTURE, CBSE_UPDATE
    val sourceName: String,
    val sourceUrl: String,
    val academicSession: String = "2025-26",
    val publicationDate: String = "2025",
    val lastVerified: String = "Verified",
    val trustLevel: String = "OFFICIAL_CBSE", // OFFICIAL_CBSE, VERIFIED_EDUCATIONAL, THIRD_PARTY
    val duration: String = "",
    val tags: String = "",
    val isSaved: Boolean = false,
    val progressStatus: String = "Not Started" // Not Started, In Progress, Completed
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String, // Consistency, Syllabus, Focus, Revision, Mastery
    val isUnlocked: Boolean = false,
    val unlockedDate: String = ""
)

@Entity(tableName = "widget_preferences")
data class WidgetPreference(
    @PrimaryKey val widgetKey: String,
    val title: String,
    val isVisible: Boolean = true,
    val orderIndex: Int = 0
)
