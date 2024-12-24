package edu.vt.mobiledev.dreamcatcher.database
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.vt.mobiledev.dreamcatcher.Dream
import edu.vt.mobiledev.dreamcatcher.DreamEntry

@Database(entities = [Dream::class, DreamEntry::class], version = 1)
@TypeConverters(DreamTypeConverters::class)
abstract class DreamDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao

}

