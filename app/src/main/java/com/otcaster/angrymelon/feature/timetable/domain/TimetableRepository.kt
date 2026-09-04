package com.otcaster.angrymelon.feature.timetable.domain

import com.otcaster.angrymelon.core.database.*
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {
    fun activeSemester(): Flow<SemesterEntity?>
    fun courses(semesterId: Long): Flow<List<CourseEntity>>
    fun sessions(semesterId: Long): Flow<List<SessionRow>>
    suspend fun createSemester(name: String): Long
    suspend fun createCourse(semesterId: Long, name: String, code: String, teacher: String): Long
    suspend fun addSession(session: ClassSessionEntity): Long
}
