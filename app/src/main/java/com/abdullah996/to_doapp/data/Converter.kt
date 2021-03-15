package com.abdullah996.to_doapp.data

import androidx.room.TypeConverter
import com.abdullah996.to_doapp.data.models.Priority

class Converter {
    @TypeConverter
    fun fromPriority(priority: Priority):String{
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}