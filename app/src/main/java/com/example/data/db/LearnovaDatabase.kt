package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LearnovaDao
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

@Database(
    entities = [
        UserProfile::class,
        Subject::class,
        Chapter::class,
        Topic::class,
        Exam::class,
        StudyTask::class,
        FocusSession::class,
        Mistake::class,
        Goal::class,
        Assignment::class,
        AcademicResource::class,
        Achievement::class,
        WidgetPreference::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LearnovaDatabase : RoomDatabase() {
    abstract fun learnovaDao(): LearnovaDao

    companion object {
        @Volatile
        private var INSTANCE: LearnovaDatabase? = null

        fun getDatabase(context: Context): LearnovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LearnovaDatabase::class.java,
                    "learnova_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
