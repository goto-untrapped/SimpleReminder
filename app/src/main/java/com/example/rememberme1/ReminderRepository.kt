package com.example.rememberme1

import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val allReminders: Flow<List<Reminder>> = reminderDao.getAllReminders()

    suspend fun insert(reminder: Reminder) {
        reminderDao.insert(reminder)
    }

}