# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Correção Crítica: Vigência Temporal de Metas e Submetas (Histórico Preservado)

OpenCode #2 (DEV), siga estas instruções passo a passo:

### Contexto do Problema:
Quando o usuário cria uma meta em uma data futura (ex: 05/11/2026), essa meta atualmente aparece nos dias passados (04/11, 01/11...), porque as tabelas `metas_dia` e `submetas` não tinham filtro de data.

---

### 1. Atualizar Entidades:
Em `app/src/main/java/com/rotina/data/Meta.kt`:
```kotlin
@Entity(tableName = "metas_dia")
data class Meta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val ordem: Int,
    @ColumnInfo(defaultValue = "") val alvoTexto: String = "",
    @ColumnInfo(defaultValue = "0") val dataInicio: Long = 0L,
    @ColumnInfo(defaultValue = "NULL") val dataFim: Long? = null,
)
```

Em `app/src/main/java/com/rotina/data/Submeta.kt`:
```kotlin
@Entity(
    tableName = "submetas",
    indices = [Index("metaId")],
)
data class Submeta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val metaId: Long,
    val nome: String,
    val ordem: Int,
    @ColumnInfo(defaultValue = "0") val dataInicio: Long = 0L,
    @ColumnInfo(defaultValue = "NULL") val dataFim: Long? = null,
)
```

---

### 2. No `app/src/main/java/com/rotina/data/RotinaDb.kt`:
- Aumentar `version = 5`.
- Criar `MIGRACAO_4_5`:
```kotlin
val MIGRACAO_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE metas_dia ADD COLUMN dataInicio INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE metas_dia ADD COLUMN dataFim INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE submetas ADD COLUMN dataInicio INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE submetas ADD COLUMN dataFim INTEGER DEFAULT NULL")
    }
}
```
- Adicionar `MIGRACAO_4_5` no `.addMigrations(...)` do Room.

---

### 3. No `app/src/main/java/com/rotina/data/RotinaDao.kt`:
- Atualizar `checklistDoDia(data: Long)`:
```sql
SELECT m.id AS id, m.nome AS nome, m.ordem AS ordem,
       m.alvoTexto AS alvoTexto, c.concluida AS concluida
FROM metas_dia m
LEFT JOIN checklist c ON c.metaId = m.id AND c.data = :data
WHERE m.dataInicio <= :data AND (m.dataFim IS NULL OR m.dataFim >= :data)
ORDER BY m.ordem ASC
```

- Atualizar `submetasDoDia(data: Long)`:
```sql
SELECT s.id AS id, s.metaId AS metaId, s.nome AS nome, s.ordem AS ordem,
       m.ordem AS metaOrdem, sd.concluida AS concluida
FROM submetas s
JOIN metas_dia m ON m.id = s.metaId
LEFT JOIN submeta_dia sd ON sd.submetaId = s.id AND sd.data = :data
WHERE s.dataInicio <= :data AND (s.dataFim IS NULL OR s.dataFim >= :data)
  AND m.dataInicio <= :data AND (m.dataFim IS NULL OR m.dataFim >= :data)
ORDER BY m.ordem ASC, s.ordem ASC
```

- Adicionar consultas para a tela de edição respeitando a data selecionada:
```kotlin
    @Query("""
        SELECT * FROM metas_dia 
        WHERE dataInicio <= :data AND (dataFim IS NULL OR dataFim >= :data) 
        ORDER BY ordem ASC
    """)
    fun metasNaData(data: Long): Flow<List<Meta>>

    @Query("""
        SELECT * FROM submetas 
        WHERE dataInicio <= :data AND (dataFim IS NULL OR dataFim >= :data) 
        ORDER BY metaId ASC, ordem ASC
    """)
    fun submetasNaData(data: Long): Flow<List<Submeta>>

    @Query("UPDATE metas_dia SET dataFim = :dataFim WHERE id = :id")
    suspend fun encerrarMeta(id: Long, dataFim: Long)

    @Query("UPDATE submetas SET dataFim = :dataFim WHERE id = :id")
    suspend fun encerrarSubmeta(id: Long, dataFim: Long)
```

---

### 4. No `app/src/main/java/com/rotina/ui/RotinaViewModel.kt`:
- Conectar `metasCfg` e `submetasCfg` reativamente à `dataSelecionada`:
```kotlin
    val metasCfg: StateFlow<List<Meta>> = dataSelecionada
        .flatMapLatest { dao.metasNaData(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val submetasCfg: StateFlow<List<Submeta>> = dataSelecionada
        .flatMapLatest { dao.submetasNaData(it) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())
```
- Ao criar meta ou submeta:
```kotlin
    fun criarMeta(nome: String, alvoTexto: String) {
        viewModelScope.launch {
            val proxima = (metasCfg.value.maxOfOrNull { it.ordem } ?: 0) + 1
            dao.inserirMeta(
                Meta(
                    nome = nome,
                    ordem = proxima,
                    alvoTexto = alvoTexto,
                    dataInicio = dataSelecionada.value
                )
            )
        }
    }

    fun criarSubmeta(metaId: Long, nome: String) {
        viewModelScope.launch {
            val proxima = (submetasCfg.value.filter { it.metaId == metaId }.maxOfOrNull { it.ordem } ?: 0) + 1
            dao.inserirSubmeta(
                Submeta(
                    metaId = metaId,
                    nome = nome,
                    ordem = proxima,
                    dataInicio = dataSelecionada.value
                )
            )
        }
    }
```
- Ao apagar meta:
Se `meta.dataInicio == dataSelecionada.value`, pode chamar `apagarMetaComDependencias(meta)`.
Se `meta.dataInicio < dataSelecionada.value`, faz soft delete: `dao.encerrarMeta(meta.id, dataSelecionada.value - 1)` (assim ela deixa de aparecer hoje e no futuro, mas o histórico anterior fica 100% preservado!).
Mesma lógica para submeta.

---

### 5. Compilar e Testar:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Instale e teste no TECNO KL4:
1. No app, vá pelo calendário para 05/11/2026.
2. Crie uma meta nova (ex: "Meta Teste 05").
3. Verifique se ela aparece no dia 05/11/2026.
4. Volte pelo calendário para o dia de hoje (03/11/2026) ou anteriores.
5. **CONFIRME:** A "Meta Teste 05" **NÃO PODE APARECER** nos dias anteriores a 05/11!

### 6. Commit:
```powershell
git add -A
git commit -m "fix: vigencia temporal de metas e submetas com dataInicio e dataFim (migration 4 para 5)"
```

Se houver erro, pare e mostre o erro completo.
