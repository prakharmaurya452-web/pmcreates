package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.DailyBriefing
import com.example.data.ai.LearnovaAiService
import com.example.data.ai.NextBestAction
import com.example.data.ai.ReadinessBreakdown
import com.example.data.model.AcademicResource
import com.example.data.model.Achievement
import com.example.data.model.Assignment
import com.example.data.model.Chapter
import com.example.data.model.Exam
import com.example.data.model.FocusSession
import com.example.data.model.Goal
import com.example.data.model.Mistake
import com.example.data.model.StudyTask
import com.example.data.model.Subject
import com.example.data.model.Topic
import com.example.data.model.UserProfile
import com.example.data.model.WidgetPreference
import com.example.data.repository.LearnovaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class QuadTuple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

class LearnovaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LearnovaRepository(application)
    private val aiService = LearnovaAiService()

    // Base Database flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    val subjects: StateFlow<List<Subject>> = repository.allSubjects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val chapters: StateFlow<List<Chapter>> = repository.allChapters.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val topics: StateFlow<List<Topic>> = repository.allTopics.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val exams: StateFlow<List<Exam>> = repository.allExams.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val studyTasks: StateFlow<List<StudyTask>> = repository.allStudyTasks.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val focusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val mistakes: StateFlow<List<Mistake>> = repository.allMistakes.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val goals: StateFlow<List<Goal>> = repository.allGoals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val assignments: StateFlow<List<Assignment>> = repository.allAssignments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val resources: StateFlow<List<AcademicResource>> = repository.allResources.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val achievements: StateFlow<List<Achievement>> = repository.allAchievements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val widgetPreferences: StateFlow<List<WidgetPreference>> = repository.allWidgetPreferences.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Derived AI & Computation Flows
    val nextBestAction: StateFlow<NextBestAction?> = combine(
        combine(topics, exams, studyTasks, mistakes) { tops, exms, tsks, msts ->
            QuadTuple(tops, exms, tsks, msts)
        },
        assignments,
        userProfile
    ) { (tops, exms, tsks, msts), assgns, prof ->
        aiService.recommendNextAction(
            tops, exms, tsks, msts, assgns, prof?.availableMinutesPerDay ?: 150
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readinessBreakdown: StateFlow<ReadinessBreakdown?> = combine(
        topics, focusSessions, exams
    ) { tops, sess, exms ->
        aiService.calculateReadiness(tops, sess, exms)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyBriefing: StateFlow<DailyBriefing?> = combine(
        topics, studyTasks, exams, userProfile
    ) { tops, tsks, exms, prof ->
        aiService.generateDailyBriefing(tops, tsks, exms, prof)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Today's study minutes
    val todayStudyMinutes: StateFlow<Int> = focusSessions.combine(userProfile) { sessions, _ ->
        val startOfDay = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L)
        sessions.filter { it.timestamp >= startOfDay }.sumOf { it.actualMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    // Weak topics
    val weakTopics: StateFlow<List<Topic>> = topics.combine(userProfile) { list, _ ->
        list.filter { it.accuracyPercent < 70 || it.confidenceLevel <= 2 || it.priority == "Urgent" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Revision due today
    val revisionDueTopics: StateFlow<List<Topic>> = topics.combine(userProfile) { list, _ ->
        val now = System.currentTimeMillis()
        list.filter {
            it.status == "Revised" || it.status == "Learning" || it.status == "Practicing"
        }.filter {
            val days = if (it.lastRevisedTimestamp > 0) (now - it.lastRevisedTimestamp) / 86400000L else 7L
            days >= 4
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // Focus Timer State & Engine
    // -------------------------------------------------------------
    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerMode = MutableStateFlow("Pomodoro") // Pomodoro, Short Break, Long Break, Custom
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private val _activeSubject = MutableStateFlow("Science")
    val activeSubject: StateFlow<String> = _activeSubject.asStateFlow()

    private val _activeTopic = MutableStateFlow("Electricity — Power & Commercial Units")
    val activeTopic: StateFlow<String> = _activeTopic.asStateFlow()

    private val _showSessionCompleteDialog = MutableStateFlow(false)
    val showSessionCompleteDialog: StateFlow<Boolean> = _showSessionCompleteDialog.asStateFlow()

    private var timerJob: Job? = null

    fun selectTopicForTimer(subject: String, topic: String) {
        _activeSubject.value = subject
        _activeTopic.value = topic
    }

    fun startTimer() {
        if (_timerRunning.value) return
        _timerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(1000L)
                _timerRemainingSeconds.value -= 1
            }
            if (_timerRemainingSeconds.value <= 0) {
                _timerRunning.value = false
                _showSessionCompleteDialog.value = true
            }
        }
    }

    fun pauseTimer() {
        _timerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    fun setTimerDurationMinutes(minutes: Int, modeName: String = "Custom") {
        pauseTimer()
        _timerMode.value = modeName
        _timerTotalSeconds.value = minutes * 60
        _timerRemainingSeconds.value = minutes * 60
    }

    fun dismissCompleteDialog() {
        _showSessionCompleteDialog.value = false
        resetTimer()
    }

    fun saveCompletedSession(rating: Int, notes: String) {
        viewModelScope.launch {
            val plannedMin = _timerTotalSeconds.value / 60
            val actualMin = (plannedMin - (_timerRemainingSeconds.value / 60)).coerceAtLeast(1)
            repository.saveFocusSession(
                subjectName = _activeSubject.value,
                chapterName = "",
                topicTitle = _activeTopic.value,
                plannedMinutes = plannedMin,
                actualMinutes = actualMin,
                rating = rating,
                notes = notes,
                mode = _timerMode.value
            )
            _showSessionCompleteDialog.value = false
            resetTimer()
        }
    }

    // -------------------------------------------------------------
    // AI Chat Coach
    // -------------------------------------------------------------
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                sender = "coach",
                text = "Hello! I am your Learnova AI Coach. I continuously analyze your syllabus progress, accuracy, upcoming board exams, and study habits.\n\nAsk me anything like:\n• \"What should I study tonight?\"\n• \"Which Maths chapters need attention?\"\n• \"I have only 45 minutes, what should I study?\""
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isCoachThinking = MutableStateFlow(false)
    val isCoachThinking: StateFlow<Boolean> = _isCoachThinking.asStateFlow()

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg
        _isCoachThinking.value = true

        viewModelScope.launch {
            val reply = aiService.askStudyCoach(
                userMessage = text,
                profile = userProfile.value,
                topics = topics.value,
                exams = exams.value,
                mistakes = mistakes.value,
                sessions = focusSessions.value
            )
            _isCoachThinking.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "coach", text = reply)
        }
    }

    // -------------------------------------------------------------
    // Replanning & Tasks
    // -------------------------------------------------------------
    private val _replanMessage = MutableStateFlow<String?>(null)
    val replanMessage: StateFlow<String?> = _replanMessage.asStateFlow()

    fun triggerReplanning(availableMinutes: Int) {
        viewModelScope.launch {
            val currentTasks = studyTasks.value
            val (updatedTasks, explanation) = aiService.replanTasks(currentTasks, availableMinutes)
            _replanMessage.value = explanation
            updatedTasks.forEach { repository.addStudyTask(it) }
        }
    }

    fun dismissReplanMessage() {
        _replanMessage.value = null
    }

    fun toggleTask(task: StudyTask) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun addStudyTask(
        subject: String,
        chapter: String,
        topic: String,
        type: String,
        duration: Int,
        priority: String
    ) {
        viewModelScope.launch {
            val task = StudyTask(
                id = "task_${java.util.UUID.randomUUID().toString().take(8)}",
                subject = subject,
                chapter = chapter,
                topic = topic,
                activityType = type,
                durationMinutes = duration,
                priority = priority,
                plannedDate = "2026-09-12",
                isCompleted = false,
                reason = "Added by student to schedule"
            )
            repository.addStudyTask(task)
        }
    }

    // -------------------------------------------------------------
    // Onboarding & Profile
    // -------------------------------------------------------------
    fun completeOnboarding(
        nickname: String,
        grade: String,
        curriculum: String,
        targetMinutes: Int,
        examName: String,
        examDateDaysFromNow: Int,
        goal: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            val updated = current.copy(
                name = nickname.ifBlank { "Student" },
                grade = grade,
                curriculum = curriculum,
                availableMinutesPerDay = targetMinutes,
                academicGoal = goal.ifBlank { "Score 95%+ in Board Examinations" },
                isOnboarded = true
            )
            repository.updateProfile(updated)

            if (examName.isNotBlank()) {
                val examDateMs = System.currentTimeMillis() + (examDateDaysFromNow * 86400000L)
                repository.addExam(examName, "All Core Subjects", examDateMs, 95)
            }
        }
    }

    fun updateTheme(themeMode: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.updateProfile(current.copy(themeMode = themeMode))
        }
    }

    // -------------------------------------------------------------
    // Syllabus Operations
    // -------------------------------------------------------------
    fun addSubject(name: String, color: String, targetHours: Int) {
        viewModelScope.launch {
            repository.addSubject(name, "book", color, targetHours)
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun addChapter(subjectId: String, title: String) {
        viewModelScope.launch {
            repository.addChapter(subjectId, title)
        }
    }

    fun deleteChapter(id: String) {
        viewModelScope.launch {
            repository.deleteChapter(id)
        }
    }

    fun addTopic(chapterId: String, subjectId: String, title: String, estimatedMinutes: Int) {
        viewModelScope.launch {
            repository.addTopic(chapterId, subjectId, title, estimatedMinutes)
        }
    }

    fun updateTopicStatus(topic: Topic, newStatus: String, newAccuracy: Int, newConfidence: Int) {
        viewModelScope.launch {
            val updated = topic.copy(
                status = newStatus,
                accuracyPercent = newAccuracy,
                confidenceLevel = newConfidence,
                lastStudiedTimestamp = System.currentTimeMillis(),
                lastRevisedTimestamp = if (newStatus == "Revised" || newStatus == "Mastered") System.currentTimeMillis() else topic.lastRevisedTimestamp
            )
            repository.updateTopic(updated)
        }
    }

    fun deleteTopic(id: String) {
        viewModelScope.launch {
            repository.deleteTopic(id)
        }
    }

    // -------------------------------------------------------------
    // Mistakes
    // -------------------------------------------------------------
    fun addMistake(
        question: String,
        studentAnswer: String,
        correctAnswer: String,
        explanation: String,
        subject: String,
        chapter: String,
        topic: String,
        type: String
    ) {
        viewModelScope.launch {
            val mistake = Mistake(
                id = "mis_${java.util.UUID.randomUUID().toString().take(8)}",
                question = question,
                studentAnswer = studentAnswer,
                correctAnswer = correctAnswer,
                explanation = explanation,
                subject = subject,
                chapter = chapter,
                topic = topic,
                mistakeType = type,
                status = "Unresolved"
            )
            repository.addMistake(mistake)
        }
    }

    fun updateMistakeStatus(mistake: Mistake, newStatus: String) {
        viewModelScope.launch {
            repository.updateMistake(mistake.copy(status = newStatus))
        }
    }

    fun deleteMistake(id: String) {
        viewModelScope.launch {
            repository.deleteMistake(id)
        }
    }

    // -------------------------------------------------------------
    // Exams, Goals, Assignments
    // -------------------------------------------------------------
    fun addExam(name: String, subjects: String, daysFromNow: Int, targetScore: Int) {
        viewModelScope.launch {
            val dateMs = System.currentTimeMillis() + (daysFromNow * 86400000L)
            repository.addExam(name, subjects, dateMs, targetScore)
        }
    }

    fun deleteExam(id: String) {
        viewModelScope.launch {
            repository.deleteExam(id)
        }
    }

    fun addGoal(title: String, target: String, deadline: String, category: String) {
        viewModelScope.launch {
            val goal = Goal(
                id = "goal_${java.util.UUID.randomUUID().toString().take(8)}",
                title = title,
                target = target,
                deadline = deadline,
                progress = 0,
                priority = "High",
                status = "Active",
                category = category
            )
            repository.addGoal(goal)
        }
    }

    fun addAssignment(title: String, subject: String, daysUntilDue: Int, duration: Int) {
        viewModelScope.launch {
            val dueMs = System.currentTimeMillis() + (daysUntilDue * 86400000L)
            val assignment = Assignment(
                id = "ass_${java.util.UUID.randomUUID().toString().take(8)}",
                title = title,
                subject = subject,
                dueDateTimestamp = dueMs,
                estimatedMinutes = duration,
                priority = "High",
                status = "Pending"
            )
            repository.addAssignment(assignment)
        }
    }

    // -------------------------------------------------------------
    // Academic Resources
    // -------------------------------------------------------------
    fun toggleResourceSaved(resource: AcademicResource) {
        viewModelScope.launch {
            repository.toggleResourceSaved(resource)
        }
    }

    fun updateResourceProgress(resource: AcademicResource, newStatus: String) {
        viewModelScope.launch {
            repository.updateResourceProgress(resource, newStatus)
        }
    }

    fun addResourceToPlan(resource: AcademicResource) {
        viewModelScope.launch {
            val task = StudyTask(
                id = "task_${java.util.UUID.randomUUID().toString().take(8)}",
                subject = resource.subject,
                chapter = resource.chapter,
                topic = resource.topic,
                activityType = when (resource.resourceType) {
                    "UDAAN_LECTURE" -> "Concept Lecture"
                    "PYQ" -> "Previous Year Paper"
                    "SAMPLE_PAPER" -> "Sample Paper"
                    else -> "Academic Resource Study"
                },
                durationMinutes = 35,
                priority = "High",
                plannedDate = "2026-09-12",
                isCompleted = false,
                reason = "Added from verified resources: ${resource.title}"
            )
            repository.addStudyTask(task)
        }
    }

    // -------------------------------------------------------------
    // Widget Preferences
    // -------------------------------------------------------------
    fun toggleWidget(pref: WidgetPreference) {
        viewModelScope.launch {
            repository.updateWidgetPreference(pref.copy(isVisible = !pref.isVisible))
        }
    }

    fun resetWidgets() {
        viewModelScope.launch {
            repository.resetWidgetPreferences()
        }
    }
}
