package com.otcaster.angrymelon.core.database

import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "semesters")
data class SemesterEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val isActive: Boolean = false, val archived: Boolean = false)
@Entity(tableName = "courses", foreignKeys = [ForeignKey(entity = SemesterEntity::class, parentColumns = ["id"], childColumns = ["semesterId"], onDelete = ForeignKey.CASCADE)], indices = [Index("semesterId")])
data class CourseEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val semesterId: Long, val name: String, val code: String = "", val teacherName: String = "", val colorSeed: Long = 0)
@Entity(tableName = "class_sessions", foreignKeys = [ForeignKey(entity = CourseEntity::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)], indices = [Index("courseId")])
data class ClassSessionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val courseId: Long, val dayOfWeek: Int, val startMinutes: Int, val endMinutes: Int, val room: String = "", val sessionType: String = "LECTURE")

@Dao interface TimetableDao {
    @Query("SELECT * FROM semesters WHERE isActive = 1 AND archived = 0 LIMIT 1") fun activeSemester(): Flow<SemesterEntity?>
    @Query("SELECT * FROM courses WHERE semesterId = :semesterId ORDER BY name") fun courses(semesterId: Long): Flow<List<CourseEntity>>
    @Query("SELECT s.*, c.name AS courseName, c.code AS courseCode, c.colorSeed AS colorSeed FROM class_sessions s JOIN courses c ON c.id = s.courseId WHERE c.semesterId = :semesterId ORDER BY s.dayOfWeek, s.startMinutes") fun sessions(semesterId: Long): Flow<List<SessionRow>>
    @Insert suspend fun insertSemester(semester: SemesterEntity): Long
    @Insert suspend fun insertCourse(course: CourseEntity): Long
    @Insert suspend fun insertSession(session: ClassSessionEntity): Long
    @Query("UPDATE semesters SET isActive = 0") suspend fun clearActiveSemester()
    @Query("UPDATE semesters SET isActive = 1 WHERE id = :semesterId") suspend fun setActiveSemester(semesterId: Long)
}
data class SessionRow(val id: Long, val courseId: Long, val dayOfWeek: Int, val startMinutes: Int, val endMinutes: Int, val room: String, val sessionType: String, val courseName: String, val courseCode: String, val colorSeed: Long)

@Database(entities = [SemesterEntity::class, CourseEntity::class, ClassSessionEntity::class], version = 1, exportSchema = true)
abstract class TimetableDatabase : RoomDatabase() { abstract fun timetableDao(): TimetableDao }
