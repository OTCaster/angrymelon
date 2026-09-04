package com.otcaster.angrymelon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.otcaster.angrymelon.core.designsystem.CadenceTheme
import com.otcaster.angrymelon.core.database.SessionRow
import com.otcaster.angrymelon.feature.timetable.presentation.TimetableViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek

@AndroidEntryPoint class MainActivity : ComponentActivity() { override fun onCreate(state: Bundle?) { super.onCreate(state); setContent { CadenceTheme { CadenceApp() } } } }
private enum class Destination(val label: String) { TODAY("Today"), WEEK("Week"), COURSES("Courses"), SETTINGS("Settings") }
@OptIn(ExperimentalMaterial3Api::class) @Composable private fun CadenceApp(vm: TimetableViewModel = hiltViewModel()) {
    val ui by vm.state.collectAsState(); var destination by remember { mutableStateOf(Destination.TODAY) }; var adding by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text(ui.semester?.name ?: "Cadence") }) }, bottomBar = { NavigationBar { Destination.entries.forEach { page -> NavigationBarItem(selected = page == destination, onClick = { destination = page }, icon = { Icon(if (page == Destination.TODAY) Icons.Outlined.Home else if (page == Destination.WEEK) Icons.Outlined.CalendarMonth else if (page == Destination.COURSES) Icons.Outlined.MenuBook else Icons.Outlined.Settings, page.label) }, label = { Text(page.label) }) } } }, floatingActionButton = { if (destination != Destination.SETTINGS) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Outlined.Add, "Add") } }) { padding ->
        Box(Modifier.padding(padding)) { if (ui.semester == null) Onboarding(vm) else when(destination) { Destination.TODAY -> Today(ui.sessions); Destination.WEEK -> Week(ui.sessions); Destination.COURSES -> Courses(ui.courses.map { it.name to it.code }); Destination.SETTINGS -> Settings() }
            if (adding) AddSheet(destination, ui.courses.map { it.id to it.name }, vm) { adding = false }
        }
    }
}
@Composable private fun Onboarding(vm: TimetableViewModel) { var name by remember { mutableStateOf("Fall 2026") }; Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Your schedule, in rhythm.", style = MaterialTheme.typography.headlineMedium); Text("Create your first semester to start building an offline timetable."); OutlinedTextField(name, { name = it }, label = { Text("Semester name") }, singleLine = true); Button(onClick = { vm.createSemester(name) }) { Text("Get started") } } }
@Composable private fun Today(sessions: List<SessionRow>) { Agenda("Today", sessions.filter { it.dayOfWeek == java.time.LocalDate.now().dayOfWeek.value }) }
@Composable private fun Week(sessions: List<SessionRow>) { LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { DayOfWeek.entries.forEach { day -> item { Text(day.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleLarge) }; items(sessions.filter { it.dayOfWeek == day.value }, key = { it.id }) { SessionCard(it) } } } }
@Composable private fun Agenda(title: String, sessions: List<SessionRow>) { if (sessions.isEmpty()) Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) { Text("No classes $title.lowercase()", style = MaterialTheme.typography.headlineSmall); Text("Enjoy the break, or add a class session.") } else LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { item { Text(title, style = MaterialTheme.typography.headlineMedium) }; items(sessions, key = { it.id }) { SessionCard(it) } } }
@Composable private fun SessionCard(s: SessionRow) { ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(s.courseName, style = MaterialTheme.typography.titleMedium); Text("${formatTime(s.startMinutes)}–${formatTime(s.endMinutes)} · ${s.room.ifBlank { "Room TBA" }}"); if (s.courseCode.isNotBlank()) Text(s.courseCode, style = MaterialTheme.typography.labelMedium) } } }
@Composable private fun Courses(courses: List<Pair<String, String>>) { if (courses.isEmpty()) Box(Modifier.fillMaxSize().padding(24.dp)) { Text("No courses yet. Use the + button to add one.") } else LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(courses) { (name, code) -> ElevatedCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(name, style = MaterialTheme.typography.titleMedium); Text(code) } } } } }
@Composable private fun Settings() { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { Text("Settings", style = MaterialTheme.typography.headlineMedium); Text("Cadence uses your device’s dynamic color and keeps your timetable on this device."); ListItem(headlineContent = { Text("Theme") }, supportingContent = { Text("System dynamic color") }); ListItem(headlineContent = { Text("Backup & restore") }, supportingContent = { Text("Coming next: versioned JSON exports") }) } }
@OptIn(ExperimentalMaterial3Api::class) @Composable private fun AddSheet(destination: Destination, courses: List<Pair<Long, String>>, vm: TimetableViewModel, close: () -> Unit) { var name by remember { mutableStateOf("") }; var code by remember { mutableStateOf("") }; var room by remember { mutableStateOf("") }; ModalBottomSheet(onDismissRequest = close) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { if (destination == Destination.COURSES || courses.isEmpty()) { Text("Add course", style = MaterialTheme.typography.titleLarge); OutlinedTextField(name, { name = it }, label = { Text("Course name") }); OutlinedTextField(code, { code = it }, label = { Text("Course code") }); Button(onClick = { vm.addCourse(name, code, ""); close() }, enabled = name.isNotBlank()) { Text("Save course") } } else { Text("Add class session", style = MaterialTheme.typography.titleLarge); OutlinedTextField(room, { room = it }, label = { Text("Room") }); Text("Creates a Monday 9:00–10:00 session; edit controls are the next refinement.", style = MaterialTheme.typography.bodySmall); Button(onClick = { vm.addSession(courses.first().first, 1, 540, 600, room); close() }) { Text("Save session") } } } } }
private fun formatTime(minutes: Int) = "%02d:%02d".format(minutes / 60, minutes % 60)
