package com.example.rememberme1

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalTime

class Converters {
    @TypeConverter
    fun fromDayOfWeekSetToString(days: Set<DayOfWeek>): String {
        // "MONDAY,TUESDAY,WEDNESDAY" のような文字列に変換
        return days.joinToString(",") { it.name }
    }

    @TypeConverter
    fun fromStringToDayOfWeekSet(daysString: String): Set<DayOfWeek> {
        if (daysString.isBlank()) {
            return emptySet()
        }
        return daysString.split(",").map { DayOfWeek.valueOf(it) }.toSet()
    }

    @TypeConverter
    fun fromLocalTimeToString(time: LocalTime): String {
        // "HH:mm" 形式の文字列に変換
        return time.toString()
    }

    @TypeConverter
    fun fromStringToLocalTime(timeString: String): LocalTime {
        if (timeString.isBlank()) {
            return LocalTime.now()
        }
        return LocalTime.parse(timeString)
    }
}