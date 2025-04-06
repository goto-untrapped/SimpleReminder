package com.example.rememberme1

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeTypeAdapter : TypeAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm") // フォーマットを指定

    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(input: JsonReader): LocalTime? {
        if (input.peek() == com.google.gson.stream.JsonToken.NULL) {
            input.nextNull()
            return null
        }
        return LocalTime.parse(input.nextString(), formatter)
    }
}