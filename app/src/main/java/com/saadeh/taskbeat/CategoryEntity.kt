package com.saadeh.taskbeat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo("Key")
    val name: String,
    @ColumnInfo("is_selected")
    val isSelected: Boolean
)
