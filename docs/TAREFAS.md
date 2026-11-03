# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 4 — Tarefa 4.3: Modelagem e Persistência do Diário de Treino Realizado

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. Criar `app/src/main/java/com/rotina/data/RegistroTreino.kt`:
```kotlin
package com.rotina.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "registro_treino_dia")
data class RegistroTreinoDia(
    @PrimaryKey val data: Long,
    val concluido: Boolean = false,
    val alteracoesRotina: String = "",
    val fezCardio: Boolean = false,
    val tipoCardio: String = "",
    val cardioMinutos: Int = 0
)

@Entity(
    tableName = "exercicios_registro",
    indices = [Index(value = ["data"])]
)
data class ExercicioRegistro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val data: Long,
    val nome: String,
    val series: Int,
    val repeticoes: String,
    val carga: String,
    val ordem: Int = 0
)
```

### 2. No `app/src/main/java/com/rotina/data/RotinaDao.kt`, adicionar:
```kotlin
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
```

### 3. No `app/src/main/java/com/rotina/data/RotinaDb.kt`:
- Aumentar `version = 4` e incluir `RegistroTreinoDia::class` e `ExercicioRegistro::class` nas entities do `@Database`.
- Criar a migração `MIGRACAO_3_4`:
```kotlin
val MIGRACAO_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `registro_treino_dia` (
                `data` INTEGER NOT NULL,
                `concluido` INTEGER NOT NULL,
                `alteracoesRotina` TEXT NOT NULL,
                `fezCardio` INTEGER NOT NULL,
                `tipoCardio` TEXT NOT NULL,
                `cardioMinutos` INTEGER NOT NULL,
                PRIMARY KEY(`data`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `exercicios_registro` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `data` INTEGER NOT NULL,
                `nome` TEXT NOT NULL,
                `series` INTEGER NOT NULL,
                `repeticoes` TEXT NOT NULL,
                `carga` TEXT NOT NULL,
                `ordem` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercicios_registro_data` ON `exercicios_registro` (`data`)")
    }
}
```
- Registrar `.addMigrations(MIGRACAO_1_2, MIGRACAO_2_3, MIGRACAO_3_4)` no construtor do banco.

### 4. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```
Validar no aparelho real (TECNO KL4).

### 5. Commit:
```powershell
git add -A
git commit -m "feat: persistencia de registro de treino do dia, exercicios com carga/series e cardio (tarefa 4.3)"
```

Se houver erro, pare e mostre o erro completo.
