package com.otcaster.angrymelon.core.database

import android.content.Context
import androidx.room.Room
import com.otcaster.angrymelon.feature.timetable.data.RoomTimetableRepository
import com.otcaster.angrymelon.feature.timetable.domain.TimetableRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context) = Room.databaseBuilder(context, TimetableDatabase::class.java, "cadence.db").build()
    @Provides fun dao(database: TimetableDatabase) = database.timetableDao()
    @Provides @Singleton fun repository(impl: RoomTimetableRepository): TimetableRepository = impl
}
