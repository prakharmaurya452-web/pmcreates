package com.example.data.repository

import android.content.Context
import com.example.data.db.LearnovaDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class LearnovaRepository(context: Context) {
    private val db = LearnovaDatabase.getDatabase(context)
    private val dao = db.learnovaDao()

    val userProfile: Flow<UserProfile?> = dao.getUserProfile()
    val allSubjects: Flow<List<Subject>> = dao.getAllSubjects()
    val allChapters: Flow<List<Chapter>> = dao.getAllChapters()
    val allTopics: Flow<List<Topic>> = dao.getAllTopics()
    val allExams: Flow<List<Exam>> = dao.getAllExams()
    val allStudyTasks: Flow<List<StudyTask>> = dao.getAllStudyTasks()
    val allFocusSessions: Flow<List<FocusSession>> = dao.getAllFocusSessions()
    val allMistakes: Flow<List<Mistake>> = dao.getAllMistakes()
    val allGoals: Flow<List<Goal>> = dao.getAllGoals()
    val allAssignments: Flow<List<Assignment>> = dao.getAllAssignments()
    val allResources: Flow<List<AcademicResource>> = dao.getAllResources()
    val allAchievements: Flow<List<Achievement>> = dao.getAllAchievements()
    val allWidgetPreferences: Flow<List<WidgetPreference>> = dao.getAllWidgetPreferences()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedInitialData()
        }
    }

    private suspend fun checkAndSeedInitialData() {
        val currentProfile = dao.getUserProfile().firstOrNull()
        if (currentProfile == null) {
            // Seed profile
            dao.insertUserProfile(
                UserProfile(
                    id = 1,
                    name = "Aarav Sharma",
                    grade = "Class 10",
                    curriculum = "CBSE",
                    academicSession = "2025-26",
                    availableMinutesPerDay = 150,
                    preferredStudyPeriod = "Evening",
                    academicGoal = "Score 95%+ in Board Examinations",
                    isOnboarded = true,
                    themeMode = "Dark",
                    currentStreak = 4,
                    totalXp = 620,
                    level = 3
                )
            )

            // Seed Subjects
            val maths = Subject("sub_maths", "Mathematics", "calculate", "#4F46E5", "Class 10", 7, 0)
            val science = Subject("sub_science", "Science", "biotech", "#0EA5E9", "Class 10", 7, 1)
            val sst = Subject("sub_sst", "Social Science", "public", "#F59E0B", "Class 10", 5, 2)
            val english = Subject("sub_english", "English", "menu_book", "#8B5CF6", "Class 10", 4, 3)
            dao.insertSubjects(listOf(maths, science, sst, english))

            // Seed Chapters
            val chRealNumbers = Chapter("ch_maths_1", "sub_maths", "Real Numbers", 0)
            val chPolynomials = Chapter("ch_maths_2", "sub_maths", "Polynomials", 1)
            val chLinearEq = Chapter("ch_maths_3", "sub_maths", "Pair of Linear Equations", 2)
            val chQuadratic = Chapter("ch_maths_4", "sub_maths", "Quadratic Equations", 3)
            val chTriangles = Chapter("ch_maths_5", "sub_maths", "Triangles", 4)
            val chTrig = Chapter("ch_maths_6", "sub_maths", "Introduction to Trigonometry", 5)

            val chChemReactions = Chapter("ch_sci_1", "sub_science", "Chemical Reactions & Equations", 0)
            val chAcids = Chapter("ch_sci_2", "sub_science", "Acids, Bases & Salts", 1)
            val chLifeProc = Chapter("ch_sci_3", "sub_science", "Life Processes", 2)
            val chLight = Chapter("ch_sci_4", "sub_science", "Light - Reflection & Refraction", 3)
            val chElectricity = Chapter("ch_sci_5", "sub_science", "Electricity", 4)
            val chMagnetic = Chapter("ch_sci_6", "sub_science", "Magnetic Effects of Electric Current", 5)

            dao.insertChapters(listOf(
                chRealNumbers, chPolynomials, chLinearEq, chQuadratic, chTriangles, chTrig,
                chChemReactions, chAcids, chLifeProc, chLight, chElectricity, chMagnetic
            ))

            // Seed Topics
            val topics = listOf(
                Topic("top_math_1", "ch_maths_1", "sub_maths", "Fundamental Theorem of Arithmetic", "Mastered", 100, System.currentTimeMillis() - 86400000L * 2, System.currentTimeMillis() - 86400000L * 1, 95, 5, "Low", 20),
                Topic("top_math_2", "ch_maths_1", "sub_maths", "Revisiting Irrational Numbers", "Revised", 85, System.currentTimeMillis() - 86400000L * 3, System.currentTimeMillis() - 86400000L * 2, 88, 4, "Medium", 25),
                Topic("top_math_3", "ch_maths_3", "sub_maths", "Graphical Method of Solution", "Practicing", 60, System.currentTimeMillis() - 86400000L * 5, 0L, 70, 3, "Medium", 30),
                Topic("top_math_4", "ch_maths_3", "sub_maths", "Substitution & Elimination Methods", "Learning", 50, System.currentTimeMillis() - 86400000L * 8, 0L, 64, 3, "High", 35),
                Topic("top_math_5", "ch_maths_4", "sub_maths", "Standard Form & Factorisation", "Practicing", 65, System.currentTimeMillis() - 86400000L * 4, 0L, 67, 3, "High", 30),
                Topic("top_math_6", "ch_maths_4", "sub_maths", "Nature of Roots & Quadratic Formula", "Learning", 45, System.currentTimeMillis() - 86400000L * 6, 0L, 62, 2, "Urgent", 30),
                Topic("top_sci_1", "ch_sci_3", "sub_science", "Autotrophic & Heterotrophic Nutrition", "Mastered", 100, System.currentTimeMillis() - 86400000L * 3, System.currentTimeMillis() - 86400000L * 2, 92, 5, "Low", 25),
                Topic("top_sci_2", "ch_sci_3", "sub_science", "Respiration in Organisms", "Revised", 80, System.currentTimeMillis() - 86400000L * 4, System.currentTimeMillis() - 86400000L * 2, 86, 4, "Medium", 25),
                Topic("top_sci_3", "ch_sci_5", "sub_science", "Electric Current & Circuit Potential", "Revised", 85, System.currentTimeMillis() - 86400000L * 4, System.currentTimeMillis() - 86400000L * 2, 82, 4, "Medium", 25),
                Topic("top_sci_4", "ch_sci_5", "sub_science", "Ohm's Law and Resistance Factors", "Practicing", 70, System.currentTimeMillis() - 86400000L * 5, 0L, 72, 3, "High", 30),
                Topic("top_sci_5", "ch_sci_5", "sub_science", "Heating Effect of Current & Joule's Law", "Learning", 40, System.currentTimeMillis() - 86400000L * 7, 0L, 65, 3, "High", 25),
                Topic("top_sci_6", "ch_sci_5", "sub_science", "Electric Power & Commercial Units", "Learning", 35, System.currentTimeMillis() - 86400000L * 8, 0L, 58, 2, "Urgent", 25)
            )
            dao.insertTopics(topics)

            // Seed Exams
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val examMaths = Exam("exam_1", "CBSE Class 10 Board - Mathematics", "Mathematics", now + dayMs * 45, 95, "In Progress")
            val examScience = Exam("exam_2", "CBSE Class 10 Board - Science", "Science", now + dayMs * 52, 95, "In Progress")
            dao.insertExam(examMaths)
            dao.insertExam(examScience)

            // Seed Today's Study Tasks
            val todayDate = "2026-09-12"
            val tasks = listOf(
                StudyTask(
                    id = "task_1",
                    subject = "Science",
                    chapter = "Electricity",
                    topic = "Electric Power & Commercial Units",
                    activityType = "Revision",
                    durationMinutes = 25,
                    priority = "Urgent",
                    plannedDate = todayDate,
                    isCompleted = false,
                    reason = "Your recent accuracy in this topic has declined to 58%. Exam in 52 days."
                ),
                StudyTask(
                    id = "task_2",
                    subject = "Mathematics",
                    chapter = "Quadratic Equations",
                    topic = "Nature of Roots & Quadratic Formula",
                    activityType = "Practice",
                    durationMinutes = 30,
                    priority = "High",
                    plannedDate = todayDate,
                    isCompleted = false,
                    reason = "Solve 10 questions. Accuracy is 62% vs your 81% average."
                ),
                StudyTask(
                    id = "task_3",
                    subject = "Mathematics",
                    chapter = "Linear Equations",
                    topic = "Substitution & Elimination Methods",
                    activityType = "Mistake Review",
                    durationMinutes = 25,
                    priority = "Medium",
                    plannedDate = todayDate,
                    isCompleted = true,
                    reason = "Unresolved mistake logged on coordinate signs."
                )
            )
            dao.insertStudyTasks(tasks)

            // Seed Focus Sessions (for study history & today's stats)
            val sessions = listOf(
                FocusSession("sess_1", "Mathematics", "Linear Equations", "Substitution Method", 25, 25, now - 3600000L * 3, 4, "Understood algebraic simplification cleanly.", "Pomodoro"),
                FocusSession("sess_2", "Science", "Life Processes", "Respiration in Organisms", 25, 25, now - 3600000L * 2, 5, "Good retention of aerobic vs anaerobic pathways.", "Pomodoro"),
                FocusSession("sess_3", "Mathematics", "Real Numbers", "Fundamental Theorem", 40, 45, now - dayMs * 1, 5, "Solved all NCERT Exemplar questions.", "Custom"),
                FocusSession("sess_4", "Science", "Electricity", "Ohm's Law", 25, 25, now - dayMs * 2, 4, "Graph interpretation practice.", "Pomodoro")
            )
            sessions.forEach { dao.insertFocusSession(it) }

            // Seed Mistakes
            val mistakes = listOf(
                Mistake(
                    id = "mis_1",
                    question = "An electric bulb rated 220V, 100W is operated on 110V. What is the power consumed?",
                    studentAnswer = "50 W (Assumed power is halved when voltage is halved)",
                    correctAnswer = "25 W (Resistance R = V^2/P = 484 ohm. When V=110V, P = V^2/R = 12100/484 = 25W)",
                    explanation = "Bulb resistance remains constant. Power is proportional to square of voltage (P = V^2/R), not linear.",
                    subject = "Science",
                    chapter = "Electricity",
                    topic = "Electric Power & Commercial Units",
                    timestamp = now - dayMs * 2,
                    mistakeType = "Concept",
                    status = "Unresolved"
                ),
                Mistake(
                    id = "mis_2",
                    question = "Find the roots of 2x^2 - 4x + 3 = 0 using Quadratic formula.",
                    studentAnswer = "x = (4 ± √-8) / 4 = 1 ± √2",
                    correctAnswer = "No Real Roots (Discriminant D = b^2 - 4ac = 16 - 24 = -8 < 0)",
                    explanation = "Always verify D >= 0 before writing real solutions. Negative under square root indicates no real roots exist.",
                    subject = "Mathematics",
                    chapter = "Quadratic Equations",
                    topic = "Nature of Roots & Quadratic Formula",
                    timestamp = now - dayMs * 4,
                    mistakeType = "Calculation",
                    status = "Reviewing"
                ),
                Mistake(
                    id = "mis_3",
                    question = "Solve 3x + 2y = 11 and 2x + 3y = 4 for (x + y).",
                    studentAnswer = "Calculated x and y separately, took 5 minutes.",
                    correctAnswer = "Add both equations: 5x + 5y = 15 => x + y = 3 (Takes 15 seconds)",
                    explanation = "Symmetric coefficients trick saves critical exam time in board questions.",
                    subject = "Mathematics",
                    chapter = "Pair of Linear Equations",
                    topic = "Substitution & Elimination Methods",
                    timestamp = now - dayMs * 5,
                    mistakeType = "Time Management",
                    status = "Resolved"
                )
            )
            mistakes.forEach { dao.insertMistake(it) }

            // Seed Goals
            dao.insertGoal(Goal("goal_1", "Complete Electricity Chapter Revision", "Revise all 6 topics & formulae", "2026-09-18", 65, "Urgent", "Active", "Weekly"))
            dao.insertGoal(Goal("goal_2", "Master Quadratic Equations", "Solve 50 PYQ problems", "2026-09-25", 40, "High", "Active", "Weekly"))
            dao.insertGoal(Goal("goal_3", "Score 95%+ in Science Pre-Board", "Complete full syllabus revision", "2026-10-30", 55, "High", "Active", "Exam"))

            // Seed Assignments
            dao.insertAssignment(Assignment("ass_1", "Maths NCERT Exercise 4.3 & 4.4", "Mathematics", now + dayMs * 1, 45, "High", "Pending", "Focus on nature of roots word problems"))
            dao.insertAssignment(Assignment("ass_2", "Science Electricity Numerical Worksheet", "Science", now + dayMs * 2, 40, "Medium", "Pending", "Series and parallel combination power consumption"))

            // Seed Academic Resources (Official CBSE + Verified PW Udaan)
            val resources = listOf(
                AcademicResource(
                    id = "res_1",
                    title = "Official CBSE Class 10 Secondary Curriculum 2025-26",
                    description = "Official syllabus, internal assessment guidelines, and marks weightage distribution direct from Central Board of Secondary Education.",
                    subject = "All",
                    chapter = "Curriculum",
                    topic = "Official Blueprint",
                    resourceType = "OFFICIAL_CBSE",
                    sourceName = "CBSE Academic Portal",
                    sourceUrl = "https://cbseacademic.nic.in/curriculum_2025.html",
                    academicSession = "2025-26",
                    publicationDate = "2025",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "CBSE, Official, Syllabus",
                    isSaved = true,
                    progressStatus = "Completed"
                ),
                AcademicResource(
                    id = "res_2",
                    title = "CBSE Class 10 Mathematics Standard Official Sample Paper (2024-25)",
                    description = "Official sample question paper with detailed marking scheme, answer keys and step-by-step marking rubrics.",
                    subject = "Mathematics",
                    chapter = "Sample Papers",
                    topic = "Class 10 Standard",
                    resourceType = "SAMPLE_PAPER",
                    sourceName = "CBSE Official Portal",
                    sourceUrl = "https://cbseacademic.nic.in/SQP_CLASSX_2024-25.html",
                    academicSession = "2024-25",
                    publicationDate = "2024",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "Sample Paper, Mathematics, Marking Scheme",
                    isSaved = true,
                    progressStatus = "In Progress"
                ),
                AcademicResource(
                    id = "res_3",
                    title = "CBSE Class 10 Science Official Sample Paper (2024-25)",
                    description = "Official sample questions with competency assessment criteria and experimental evaluation marking scheme.",
                    subject = "Science",
                    chapter = "Sample Papers",
                    topic = "Class 10 Science",
                    resourceType = "SAMPLE_PAPER",
                    sourceName = "CBSE Official Portal",
                    sourceUrl = "https://cbseacademic.nic.in/SQP_CLASSX_2024-25.html",
                    academicSession = "2024-25",
                    publicationDate = "2024",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "Sample Paper, Science, Marking Scheme",
                    isSaved = false,
                    progressStatus = "Not Started"
                ),
                AcademicResource(
                    id = "res_4",
                    title = "Official CBSE Board Question Papers (PYQ 2024)",
                    description = "Original question papers and marking schemes from the CBSE 2024 board examinations archives.",
                    subject = "Mathematics",
                    chapter = "Previous Year Questions",
                    topic = "Board Exam 2024",
                    resourceType = "PYQ",
                    sourceName = "CBSE Examination Archive",
                    sourceUrl = "https://www.cbse.gov.in/cbsenew/question-paper.html",
                    academicSession = "2023-24",
                    publicationDate = "2024",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "PYQ, 2024, Mathematics",
                    isSaved = true,
                    progressStatus = "Not Started"
                ),
                AcademicResource(
                    id = "res_5",
                    title = "CBSE Official Competency-Based Practice Questions",
                    description = "Official question bank developed with Educational Initiatives for Class 10 case-based and application items.",
                    subject = "Science",
                    chapter = "Competency Practice",
                    topic = "Case-Based Questions",
                    resourceType = "COMPETENCY",
                    sourceName = "CBSE Academic",
                    sourceUrl = "https://cbseacademic.nic.in/cba/index.html",
                    academicSession = "2024-25",
                    publicationDate = "2024",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "Competency, Case Studies, Science",
                    isSaved = false,
                    progressStatus = "Not Started"
                ),
                AcademicResource(
                    id = "res_6",
                    title = "Electricity Complete Chapter - Physics Wallah UDAAN",
                    description = "Verified video lecture covering Ohm's law, resistance combinations, Joule's law, and commercial electric power on PW Udaan.",
                    subject = "Science",
                    chapter = "Electricity",
                    topic = "Electric Power & Heating Effect",
                    resourceType = "UDAAN_LECTURE",
                    sourceName = "YouTube • PW UDAAN",
                    sourceUrl = "https://www.youtube.com/@UDAANClass10th",
                    academicSession = "2025",
                    publicationDate = "2024",
                    lastVerified = "Verified Channel",
                    trustLevel = "VERIFIED_EDUCATIONAL",
                    duration = "1h 45m",
                    tags = "Lecture, Udaan, Electricity, PW",
                    isSaved = true,
                    progressStatus = "In Progress"
                ),
                AcademicResource(
                    id = "res_7",
                    title = "Quadratic Equations Full Chapter - Physics Wallah UDAAN",
                    description = "Concept coverage of factorisation, completing square, and discriminant analysis by master faculty on PW Udaan.",
                    subject = "Mathematics",
                    chapter = "Quadratic Equations",
                    topic = "Nature of Roots",
                    resourceType = "UDAAN_LECTURE",
                    sourceName = "YouTube • PW UDAAN",
                    sourceUrl = "https://www.youtube.com/@UDAANClass10th",
                    academicSession = "2025",
                    publicationDate = "2024",
                    lastVerified = "Verified Channel",
                    trustLevel = "VERIFIED_EDUCATIONAL",
                    duration = "1h 30m",
                    tags = "Lecture, Udaan, Quadratic Equations, PW",
                    isSaved = false,
                    progressStatus = "Not Started"
                ),
                AcademicResource(
                    id = "res_8",
                    title = "CBSE Notification: Board Examination Schedule & Format",
                    description = "Official circular on examination timeline, answer sheet guidelines, and sample question paper releases.",
                    subject = "All",
                    chapter = "Circulars",
                    topic = "Board Updates",
                    resourceType = "CBSE_UPDATE",
                    sourceName = "CBSE Official Circulars",
                    sourceUrl = "https://www.cbse.gov.in/cbsenew/circulars.html",
                    academicSession = "2025-26",
                    publicationDate = "2025",
                    lastVerified = "Verified Official",
                    trustLevel = "OFFICIAL_CBSE",
                    tags = "Circular, Notice, Board Exam",
                    isSaved = false,
                    progressStatus = "Completed"
                )
            )
            dao.insertResources(resources)

            // Seed Achievements
            val achievements = listOf(
                Achievement("ach_1", "Learnova Pioneer", "Completed initial onboarding and established study goals", "military_tech", "Milestones", true, "2026-09-08"),
                Achievement("ach_2", "Focus Sprint Master", "Completed 5 focused study sessions without interruption", "timer", "Focus", true, "2026-09-10"),
                Achievement("ach_3", "Syllabus Explorer", "Completed topics in both Mathematics and Science", "menu_book", "Syllabus", true, "2026-09-11"),
                Achievement("ach_4", "Mistake Transformer", "Reviewed and resolved an active academic mistake", "psychology", "Mastery", true, "2026-09-11"),
                Achievement("ach_5", "Consistency Champ", "Maintained an active study streak of 3+ consecutive days", "local_fire_department", "Consistency", true, "2026-09-11"),
                Achievement("ach_6", "Spaced Revision Scholar", "Completed 10 scheduled spaced revisions on time", "update", "Revision", false, ""),
                Achievement("ach_7", "Board Exam Ready", "Attained an overall exam readiness score exceeding 85%", "stars", "Mastery", false, "")
            )
            dao.insertAchievements(achievements)

            // Seed Widget Preferences
            val widgetPrefs = listOf(
                WidgetPreference("widget_today_target", "Today's Target", true, 0),
                WidgetPreference("widget_next_best_action", "Next Best Action", true, 1),
                WidgetPreference("widget_exam_countdown", "Exam Countdown", true, 2),
                WidgetPreference("widget_revision_due", "Revision Due Today", true, 3),
                WidgetPreference("widget_weakness_alert", "Weak Topic Alerts", true, 4),
                WidgetPreference("widget_quick_actions", "Quick Actions", true, 5),
                WidgetPreference("widget_syllabus_overview", "Syllabus Progress", true, 6),
                WidgetPreference("widget_mistakes_preview", "Mistake Bank Preview", true, 7),
                WidgetPreference("widget_saved_resources", "Saved Resources", true, 8)
            )
            dao.insertWidgetPreferences(widgetPrefs)
        }
    }

    // Repository operations
    suspend fun updateProfile(profile: UserProfile) = dao.insertUserProfile(profile)

    suspend fun addSubject(name: String, icon: String, color: String, targetHours: Int) {
        val subject = Subject(
            id = "sub_${UUID.randomUUID().toString().take(8)}",
            name = name,
            iconName = icon,
            colorHex = color,
            targetHoursPerWeek = targetHours
        )
        dao.insertSubject(subject)
    }

    suspend fun deleteSubject(subjectId: String) = dao.deleteSubject(subjectId)

    suspend fun addChapter(subjectId: String, title: String) {
        val chapter = Chapter(
            id = "ch_${UUID.randomUUID().toString().take(8)}",
            subjectId = subjectId,
            title = title
        )
        dao.insertChapter(chapter)
    }

    suspend fun deleteChapter(chapterId: String) = dao.deleteChapter(chapterId)

    suspend fun addTopic(chapterId: String, subjectId: String, title: String, estimatedMinutes: Int) {
        val topic = Topic(
            id = "top_${UUID.randomUUID().toString().take(8)}",
            chapterId = chapterId,
            subjectId = subjectId,
            title = title,
            estimatedMinutes = estimatedMinutes
        )
        dao.insertTopic(topic)
    }

    suspend fun updateTopic(topic: Topic) = dao.updateTopic(topic)

    suspend fun deleteTopic(topicId: String) = dao.deleteTopic(topicId)

    suspend fun addExam(name: String, subjects: String, examDateMs: Long, targetScore: Int) {
        val exam = Exam(
            id = "exam_${UUID.randomUUID().toString().take(8)}",
            name = name,
            subjectNames = subjects,
            examDateTimestamp = examDateMs,
            targetScore = targetScore
        )
        dao.insertExam(exam)
    }

    suspend fun deleteExam(examId: String) = dao.deleteExam(examId)

    suspend fun addStudyTask(task: StudyTask) = dao.insertStudyTask(task)

    suspend fun toggleTaskCompleted(task: StudyTask) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        dao.updateStudyTask(updated)
        if (updated.isCompleted) {
            // Reward XP!
            val profile = dao.getUserProfile().firstOrNull()
            if (profile != null) {
                val newXp = profile.totalXp + 35
                val newLevel = (newXp / 250) + 1
                dao.insertUserProfile(profile.copy(totalXp = newXp, level = newLevel))
            }
        }
    }

    suspend fun deleteStudyTask(taskId: String) = dao.deleteStudyTask(taskId)

    suspend fun saveFocusSession(
        subjectName: String,
        chapterName: String,
        topicTitle: String,
        plannedMinutes: Int,
        actualMinutes: Int,
        rating: Int,
        notes: String,
        mode: String
    ) {
        val session = FocusSession(
            id = "sess_${UUID.randomUUID().toString().take(8)}",
            subjectName = subjectName,
            chapterName = chapterName,
            topicTitle = topicTitle,
            plannedMinutes = plannedMinutes,
            actualMinutes = actualMinutes,
            timestamp = System.currentTimeMillis(),
            focusRating = rating,
            notes = notes,
            mode = mode
        )
        dao.insertFocusSession(session)

        // Award XP for study session!
        val profile = dao.getUserProfile().firstOrNull()
        if (profile != null) {
            val awardedXp = actualMinutes * 2 + (rating * 5)
            val newXp = profile.totalXp + awardedXp
            val newLevel = (newXp / 250) + 1
            dao.insertUserProfile(profile.copy(totalXp = newXp, level = newLevel))
        }
    }

    suspend fun addMistake(mistake: Mistake) = dao.insertMistake(mistake)

    suspend fun updateMistake(mistake: Mistake) = dao.updateMistake(mistake)

    suspend fun deleteMistake(mistakeId: String) = dao.deleteMistake(mistakeId)

    suspend fun addGoal(goal: Goal) = dao.insertGoal(goal)

    suspend fun updateGoal(goal: Goal) = dao.updateGoal(goal)

    suspend fun deleteGoal(goalId: String) = dao.deleteGoal(goalId)

    suspend fun addAssignment(assignment: Assignment) = dao.insertAssignment(assignment)

    suspend fun updateAssignment(assignment: Assignment) = dao.updateAssignment(assignment)

    suspend fun deleteAssignment(assignmentId: String) = dao.deleteAssignment(assignmentId)

    suspend fun toggleResourceSaved(resource: AcademicResource) {
        dao.updateResource(resource.copy(isSaved = !resource.isSaved))
    }

    suspend fun updateResourceProgress(resource: AcademicResource, newStatus: String) {
        dao.updateResource(resource.copy(progressStatus = newStatus))
    }

    suspend fun updateWidgetPreference(pref: WidgetPreference) = dao.updateWidgetPreference(pref)

    suspend fun resetWidgetPreferences() {
        val widgetPrefs = listOf(
            WidgetPreference("widget_today_target", "Today's Target", true, 0),
            WidgetPreference("widget_next_best_action", "Next Best Action", true, 1),
            WidgetPreference("widget_exam_countdown", "Exam Countdown", true, 2),
            WidgetPreference("widget_revision_due", "Revision Due Today", true, 3),
            WidgetPreference("widget_weakness_alert", "Weak Topic Alerts", true, 4),
            WidgetPreference("widget_quick_actions", "Quick Actions", true, 5),
            WidgetPreference("widget_syllabus_overview", "Syllabus Progress", true, 6),
            WidgetPreference("widget_mistakes_preview", "Mistake Bank Preview", true, 7),
            WidgetPreference("widget_saved_resources", "Saved Resources", true, 8)
        )
        dao.insertWidgetPreferences(widgetPrefs)
    }
}
