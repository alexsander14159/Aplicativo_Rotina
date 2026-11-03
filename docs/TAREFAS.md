# TAREFAS — Rotina App

Atualizado: 2026-11-03

## Marco 4 — Tarefa 4.1: Persistência do Treino da Semana no Room

OpenCode #2 (DEV), siga estas instruções passo a passo:

### 1. Criar `app/src/main/java/com/rotina/data/TreinoSemana.kt`:
```kotlin
package com.rotina.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treinos_semana")
data class TreinoSemana(
    /** 1 = Segunda, 2 = Terça, ..., 7 = Domingo (DayOfWeek.value) */
    @PrimaryKey val diaSemana: Int,
    val titulo: String,
    val descricao: String = ""
)
```

### 2. Em `app/src/main/java/com/rotina/data/RotinaDao.kt`, adicionar:
```kotlin
    @Query("SELECT * FROM treinos_semana WHERE diaSemana = :diaSemana")
    fun treinoDoDia(diaSemana: Int): Flow<TreinoSemana?>

    @Query("SELECT * FROM treinos_semana ORDER BY diaSemana ASC")
    fun todosTreinos(): Flow<List<TreinoSemana>>

    @Upsert
    suspend fun salvarTreino(treino: TreinoSemana)
```

### 3. Em `app/src/main/java/com/rotina/data/RotinaDatabase.kt`:
- Aumentar `version = 3` e incluir `TreinoSemana::class` na anotação `@Database(entities = [...])`.
- Criar a migração `MIGRATION_2_3`:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `treinos_semana` (
                `diaSemana` INTEGER NOT NULL,
                `titulo` TEXT NOT NULL,
                `descricao` TEXT NOT NULL,
                PRIMARY KEY(`diaSemana`)
            )
            """.trimIndent()
        )
    }
}
```
- Adicionar `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)` no builder do Room em `construir()`.

### 4. Compilar e Testar:
Execute uma tarefa por vez:
```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

### 5. Se compilar com sucesso, faça o commit:
```powershell
git add -A
git commit -m "feat: dados do treino da semana no Room com migration 2 para 3 (tarefa 4.1)"
```

Se houver qualquer erro, pare e mostre o erro completo.
