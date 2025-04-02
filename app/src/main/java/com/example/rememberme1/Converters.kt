package com.example.rememberme1

import androidx.room.TypeConverter
import java.time.DayOfWeek

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

}