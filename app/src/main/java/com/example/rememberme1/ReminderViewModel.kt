package com.example.rememberme1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalTime

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {
    val allReminders = repository.allReminders

    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.insert(reminder)
        }
    }
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
        }
    }

    // 登録時
    var reminder by mutableStateOf(Reminder(
        title = "",
        memo = "",
        remindTime = LocalTime.now(),
        remindWeekDays = emptySet(),
        registerDatetime = "",
        updateDatetime = ""
    ))

}
