package com.example.rememberme1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    val allReminders = repository.allReminders

    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insert(reminder)
        }
    }

}