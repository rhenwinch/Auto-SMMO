package com.xcape.simplemmomod.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.xcape.simplemmomod.domain.model.User

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        const val USER_DATABASE = "user_database"
    }
}