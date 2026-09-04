package com.otcaster.angrymelon.feature.timetable.data

import com.otcaster.angrymelon.core.database.*
import com.otcaster.angrymelon.feature.timetable.domain.TimetableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomTimetableRepository @Inject constructor(private val dao: TimetableDao) : TimetableRepository {
    override fun activeSemester() = dao.activeSemester()
    override fun courses(semesterId: Long) = dao.courses(semesterId)
    override fun sessions(semesterId: Long): Flow<List<SessionRow>> = dao.sessions(semesterId)
    override suspend fun createSemester(name: String): Long { dao.clearActiveSemester(); return dao.insertSemester(SemesterEntity(name = name, isActive = true)) }
    override suspend fun createCourse(semesterId: Long, name: String, code: String, teacher: String) = dao.insertCourse(CourseEntity(semesterId = semesterId, name = name, code = code, teacherName = teacher, colorSeed = name.hashCode().toLong()))
    override suspend fun addSession(session: ClassSessionEntity) = dao.insertSession(session)
}
