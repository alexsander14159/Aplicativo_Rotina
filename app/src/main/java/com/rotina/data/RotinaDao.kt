package com.rotina.data

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Uma meta do dia e o estado dela na data consultada (concluida null = ainda sem registro hoje). */
data class EstadoMeta(
    val id: Long,
    val nome: String,
    val ordem: Int,
    @ColumnInfo(name = "alvoTexto") val alvoTexto: String,
    @ColumnInfo(name = "concluida") val concluida: Boolean?,
)

/** Uma submeta e a marcação dela no dia (inclui a ordem da meta para ordenar junto). */
data class EstadoSubmeta(
    val id: Long,
    val metaId: Long,
    val nome: String,
    val ordem: Int,
    @ColumnInfo(name = "metaOrdem") val metaOrdem: Int,
    @ColumnInfo(name = "concluida") val concluida: Boolean?,
)

@Dao
interface RotinaDao {
    @Query("SELECT * FROM metas_dia ORDER BY ordem ASC")
    fun metas(): Flow<List<Meta>>

    @Query(
        """
        SELECT m.id AS id, m.nome AS nome, m.ordem AS ordem,
               m.alvoTexto AS alvoTexto, c.concluida AS concluida
        FROM metas_dia m
        LEFT JOIN checklist c ON c.metaId = m.id AND c.data = :data
        ORDER BY m.ordem ASC
        """,
    )
    fun checklistDoDia(data: Long): Flow<List<EstadoMeta>>

    @Query(
        """
        SELECT s.id AS id, s.metaId AS metaId, s.nome AS nome, s.ordem AS ordem,
               m.ordem AS metaOrdem, sd.concluida AS concluida
        FROM submetas s
        JOIN metas_dia m ON m.id = s.metaId
        LEFT JOIN submeta_dia sd ON sd.submetaId = s.id AND sd.data = :data
        ORDER BY m.ordem ASC, s.ordem ASC
        """,
    )
    fun submetasDoDia(data: Long): Flow<List<EstadoSubmeta>>

    @Query("SELECT * FROM checklist WHERE data = :data AND metaId = :metaId")
    suspend fun obter(data: Long, metaId: Long): ChecklistDia?

    @Upsert
    suspend fun salvar(item: ChecklistDia)

    // ---- submetas: marcação diária ----

    @Query("SELECT * FROM submeta_dia WHERE data = :data AND submetaId = :submetaId")
    suspend fun submetaObter(data: Long, submetaId: Long): SubmetaDia?

    @Upsert
    suspend fun submetaSalvar(item: SubmetaDia)

    // ---- folga do dia (chave natural = data; @Upsert resolve pela própria chave) ----

    @Query("SELECT * FROM dias WHERE data = :data")
    fun diaCarregar(data: Long): Flow<Dia?>

    @Upsert
    suspend fun diaSalvar(item: Dia)

    // ---- configuração: CRUD de metas e submetas (sem FK — deleção transacional) ----

    @Upsert
    suspend fun metaSalvar(item: Meta)

    @Delete
    suspend fun metaApagar(item: Meta)

    @Upsert
    suspend fun submetaSalvarCfg(item: Submeta)

    @Delete
    suspend fun submetaApagarCfg(item: Submeta)

    @Query("DELETE FROM checklist WHERE metaId = :metaId")
    suspend fun apagarMarcasDaMeta(metaId: Long)

    @Query("DELETE FROM submeta_dia WHERE submetaId IN (SELECT id FROM submetas WHERE metaId = :metaId)")
    suspend fun apagarMarcasDasSubmetasDaMeta(metaId: Long)

    @Query("DELETE FROM submetas WHERE metaId = :metaId")
    suspend fun apagarSubmetasDaMeta(metaId: Long)

    /** Apaga a meta e tudo que depende dela, numa única transação (sem FK). */
    @Transaction
    suspend fun apagarMetaComDependencias(item: Meta) {
        apagarMarcasDaMeta(item.id)
        apagarMarcasDasSubmetasDaMeta(item.id)
        apagarSubmetasDaMeta(item.id)
        metaApagar(item)
    }
}
