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
        SELECT * FROM metas_dia
        WHERE dataInicio <= :data AND (dataFim IS NULL OR dataFim >= :data)
        ORDER BY ordem ASC
        """,
    )
    fun metasNaData(data: Long): Flow<List<Meta>>

    @Query(
        """
        SELECT * FROM submetas
        WHERE dataInicio <= :data AND (dataFim IS NULL OR dataFim >= :data)
        ORDER BY metaId ASC, ordem ASC
        """,
    )
    fun submetasNaData(data: Long): Flow<List<Submeta>>

    @Query(
        """
        SELECT m.id AS id, m.nome AS nome, m.ordem AS ordem,
               m.alvoTexto AS alvoTexto, c.concluida AS concluida
        FROM metas_dia m
        LEFT JOIN checklist c ON c.metaId = m.id AND c.data = :data
        WHERE m.dataInicio <= :data AND (m.dataFim IS NULL OR m.dataFim >= :data)
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
        WHERE s.dataInicio <= :data AND (s.dataFim IS NULL OR s.dataFim >= :data)
          AND m.dataInicio <= :data AND (m.dataFim IS NULL OR m.dataFim >= :data)
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

    // ---- diário do dia (chave natural = data) ----

    @Query("SELECT * FROM diario_dia WHERE data = :data")
    fun diarioDoDia(data: Long): Flow<DiarioDia?>

    @Upsert
    suspend fun salvarDiario(diario: DiarioDia)

    // ---- histórico de conversas com a IA (Jarvis) ----

    @Query("SELECT * FROM mensagens_ia ORDER BY criadoEm ASC")
    fun todasMensagensIa(): Flow<List<MensagemIa>>

    @Query("SELECT * FROM mensagens_ia WHERE data = :data ORDER BY criadoEm ASC")
    fun mensagensIaDoDia(data: Long): Flow<List<MensagemIa>>

    @Upsert
    suspend fun salvarMensagemIa(msg: MensagemIa)

    @Query("DELETE FROM mensagens_ia")
    suspend fun limparHistoricoIa()

    // ---- folga do dia (chave natural = data; @Upsert resolve pela própria chave) ----

    @Query("SELECT * FROM dias WHERE data = :data")
    fun diaCarregar(data: Long): Flow<Dia?>

    /** Leitura pontual da anotação do dia (usada no alternarFolga sob Mutex). */
    @Query("SELECT * FROM dias WHERE data = :data")
    suspend fun diaObter(data: Long): Dia?

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

    @Query("UPDATE metas_dia SET dataFim = :dataFim WHERE id = :id")
    suspend fun encerrarMeta(id: Long, dataFim: Long)

    @Query("UPDATE submetas SET dataFim = :dataFim WHERE id = :id")
    suspend fun encerrarSubmeta(id: Long, dataFim: Long)

    @Query("SELECT * FROM metas_dia WHERE id = :id")
    suspend fun obterMeta(id: Long): Meta?

    @Query("SELECT * FROM metas_dia ORDER BY ordem ASC")
    suspend fun metasSincronas(): List<Meta>

    @Query("SELECT COALESCE(MAX(ordem), 0) FROM metas_dia")
    suspend fun maiorOrdemMeta(): Int

    @Query("SELECT * FROM submetas ORDER BY metaId ASC, ordem ASC")
    fun submetasCfg(): Flow<List<Submeta>>

    @Query("SELECT * FROM submetas WHERE metaId = :metaId ORDER BY ordem ASC")
    suspend fun submetasDaMeta(metaId: Long): List<Submeta>

    @Query("SELECT COALESCE(MAX(ordem), 0) FROM submetas WHERE metaId = :metaId")
    suspend fun maiorOrdemSubmeta(metaId: Long): Int

    @Query("DELETE FROM submeta_dia WHERE submetaId = :submetaId")
    suspend fun apagarMarcasDaSubmeta(submetaId: Long)

    @Query("DELETE FROM checklist WHERE metaId = :metaId")
    suspend fun apagarMarcasDaMeta(metaId: Long)

    @Query("DELETE FROM submeta_dia WHERE submetaId IN (SELECT id FROM submetas WHERE metaId = :metaId)")
    suspend fun apagarMarcasDasSubmetasDaMeta(metaId: Long)

    @Query("DELETE FROM submetas WHERE metaId = :metaId")
    suspend fun apagarSubmetasDaMeta(metaId: Long)

    /** Apaga a submeta e as marcações dela, numa única transação (sem FK). */
    @Transaction
    suspend fun apagarSubmetaComMarcas(item: Submeta) {
        apagarMarcasDaSubmeta(item.id)
        submetaApagarCfg(item)
    }

    /** Apaga a meta e tudo que depende dela, numa única transação (sem FK). */
    @Transaction
    suspend fun apagarMetaComDependencias(item: Meta) {
        apagarMarcasDaMeta(item.id)
        apagarMarcasDasSubmetasDaMeta(item.id)
        apagarSubmetasDaMeta(item.id)
        metaApagar(item)
    }

    // ---- treino da semana (1 = segunda ... 7 = domingo) ----

    @Query("SELECT * FROM treinos_semana WHERE diaSemana = :diaSemana")
    fun treinoDoDia(diaSemana: Int): Flow<TreinoSemana?>

    @Query("SELECT * FROM treinos_semana ORDER BY diaSemana ASC")
    fun todosTreinos(): Flow<List<TreinoSemana>>

    @Upsert
    suspend fun salvarTreino(treino: TreinoSemana)

    // ---- registro do treino realizado no dia ----

    @Query("SELECT * FROM registro_treino_dia WHERE data = :data")
    fun registroTreinoDoDia(data: Long): Flow<RegistroTreinoDia?>

    @Query("SELECT * FROM exercicios_registro WHERE data = :data ORDER BY ordem ASC, id ASC")
    fun exerciciosDoDia(data: Long): Flow<List<ExercicioRegistro>>

    @Upsert
    suspend fun salvarRegistroTreino(registro: RegistroTreinoDia)

    @Upsert
    suspend fun salvarExercicio(exercicio: ExercicioRegistro)

    @Query("DELETE FROM exercicios_registro WHERE id = :id")
    suspend fun apagarExercicio(id: Long)
}
