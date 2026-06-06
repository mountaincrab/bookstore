package com.mountaincrab.bookstore.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mountaincrab.bookstore.data.local.dao.BookDao
import com.mountaincrab.bookstore.data.local.entity.BookEntity

@Database(
    entities = [BookEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}
