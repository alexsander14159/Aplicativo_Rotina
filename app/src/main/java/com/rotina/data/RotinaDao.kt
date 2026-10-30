package com.rotina.data

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Uma meta do dia e o estado dela na data consultada (concluida null = ainda sem registro hoje). */
data class EstadoMeta(
    val id: Long,
    val nome: String,
    val ordem: Int,
    @ColumnInfo(name = "concluida") val concluida: Boolean?,
)

@Dao
interface RotinaDao {
    @Query("SELECT * FROM metas_dia ORDER BY ordem ASC")
    fun metas(): Flow<List<Meta>>

    @Query(
        """
        SELECT m.id AS id, m.nome AS nome, m.ordem AS ordem,
               c.concluida AS concluida
        FROM metas_dia m
        LEFT JOIN checklist c ON c.metaId = m.id AND c.data = :data
        ORDER BY m.ordem ASC
        """,
    )
    fun checklistDoDia(data: Long): Flow<List<EstadoMeta>>

    @Upsert
    suspend fun salvar(item: ChecklistDia)

    @Query("SELECT * FROM checklist WHERE data = :data AND metaId = :metaId")
    suspend fun obter(data: Long, metaId: Long): ChecklistDia?
}
