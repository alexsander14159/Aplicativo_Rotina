package com.rotina.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist",
    indices = [Index(value = ["data", "metaId"], unique = true)],
)
data class ChecklistDia(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** LocalDate.toEpochDay() do aparelho. */
    val data: Long,
    val metaId: Long,
    val concluida: Boolean,
)
