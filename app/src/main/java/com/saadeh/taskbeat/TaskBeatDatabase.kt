package com.saadeh.taskbeat

import androidx.room.Database
import androidx.room.RoomDatabase

@Database([CategoryEntity::class, TaskEntity::class], version = 4)
abstract class TaskBeatDatabase: RoomDatabase() {

    abstract fun getCategoryDao(): CategoryDao
    abstract fun getTaskDao(): TaskDao
}