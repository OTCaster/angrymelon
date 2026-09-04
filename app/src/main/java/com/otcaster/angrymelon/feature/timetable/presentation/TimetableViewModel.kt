package com.otcaster.angrymelon.feature.timetable.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.otcaster.angrymelon.core.database.*
import com.otcaster.angrymelon.feature.timetable.domain.TimetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimetableUiState(val semester: SemesterEntity? = null, val courses: List<CourseEntity> = emptyList(), val sessions: List<SessionRow> = emptyList())
@HiltViewModel class TimetableViewModel @Inject constructor(private val repository: TimetableRepository) : ViewModel() {
    val state = repository.activeSemester().flatMapLatest { semester ->
        if (semester == null) flowOf(TimetableUiState()) else combine(repository.courses(semester.id), repository.sessions(semester.id)) { courses, sessions -> TimetableUiState(semester, courses, sessions) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimetableUiState())
    fun createSemester(name: String) = viewModelScope.launch { if (name.isNotBlank()) repository.createSemester(name.trim()) }
    fun addCourse(name: String, code: String, teacher: String) = viewModelScope.launch { state.value.semester?.let { repository.createCourse(it.id, name.trim(), code.trim(), teacher.trim()) } }
    fun addSession(courseId: Long, day: Int, start: Int, end: Int, room: String) = viewModelScope.launch { repository.addSession(ClassSessionEntity(courseId = courseId, dayOfWeek = day, startMinutes = start, endMinutes = end, room = room)) }
}
