package com.example.rememberme1

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "memo") val memo: String,
    @ColumnInfo(name = "remindTime") val remindTime: String,
    @ColumnInfo(name = "remindWeekDays") val remindWeekDays: Set<DayOfWeek>,
    @ColumnInfo(name = "registerDatetime") val registerDatetime: String,
    @ColumnInfo(name = "updateDatetime") val updateDatetime: String,
)
