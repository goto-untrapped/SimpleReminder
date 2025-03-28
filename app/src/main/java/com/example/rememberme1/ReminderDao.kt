package com.example.rememberme1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<Reminder>>
}