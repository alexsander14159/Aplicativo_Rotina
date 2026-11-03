package com.rotina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Meta::class,
        ChecklistDia::class,
        Submeta::class,
        SubmetaDia::class,
        Dia::class,
        TreinoSemana::class,
        RegistroTreinoDia::class,
        ExercicioRegistro::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class RotinaDb : RoomDatabase() {
    abstract fun rotinaDao(): RotinaDao

    companion object {
        @Volatile
        private var instance: RotinaDb? = null

        fun get(context: Context): RotinaDb =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): RotinaDb =
            Room.databaseBuilder(context, RotinaDb::class.java, "rotina.db")
                .addCallback(MetasSeed)
                .addMigrations(MIGRACAO_1_2, MIGRACAO_2_3, MIGRACAO_3_4)
                .build()

        /** v1 → v2: alvo do dia nas metas + tabelas de submetas e folga. Dados preservados. */
        private val MIGRACAO_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE metas_dia ADD COLUMN alvoTexto TEXT NOT NULL DEFAULT ''")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `submetas` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`metaId` INTEGER NOT NULL, " +
                        "`nome` TEXT NOT NULL, " +
                        "`ordem` INTEGER NOT NULL)",
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_submetas_metaId` ON `submetas` (`metaId`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `submeta_dia` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`data` INTEGER NOT NULL, " +
                        "`submetaId` INTEGER NOT NULL, " +
                        "`concluida` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_submeta_dia_data_submetaId` " +
                        "ON `submeta_dia` (`data`, `submetaId`)",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `dias` (" +
                        "`data` INTEGER NOT NULL, " +
                        "`folga` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`data`))",
                )
            }
        }

        /** v2 → v3: treino da semana (1 = segunda ... 7 = domingo). Dados preservados. */
        private val MIGRACAO_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `treinos_semana` (" +
                        "`diaSemana` INTEGER NOT NULL, " +
                        "`titulo` TEXT NOT NULL, " +
                        "`descricao` TEXT NOT NULL, " +
                        "PRIMARY KEY(`diaSemana`))",
                )
            }
        }

        /** v3 → v4: diário do treino realizado (exercícios, cargas, séries e cardio). Dados preservados. */
        private val MIGRACAO_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `registro_treino_dia` (" +
                        "`data` INTEGER NOT NULL, " +
                        "`concluido` INTEGER NOT NULL, " +
                        "`alteracoesRotina` TEXT NOT NULL, " +
                        "`fezCardio` INTEGER NOT NULL, " +
                        "`tipoCardio` TEXT NOT NULL, " +
                        "`cardioMinutos` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`data`))",
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `exercicios_registro` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`data` INTEGER NOT NULL, " +
                        "`nome` TEXT NOT NULL, " +
                        "`series` INTEGER NOT NULL, " +
                        "`repeticoes` TEXT NOT NULL, " +
                        "`carga` TEXT NOT NULL, " +
                        "`ordem` INTEGER NOT NULL)",
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_exercicios_registro_data` " +
                        "ON `exercicios_registro` (`data`)",
                )
            }
        }

        /** Insere as 6 metas do spec na criação do banco (primeira abertura do app). */
        private object MetasSeed : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val metas = listOf(
                    "Horas de estudo",
                    "Água",
                    "Questões feitas",
                    "Academia",
                    "Dias produtivos",
                    "Comer direito",
                )
                metas.forEachIndexed { i, nome ->
                    db.execSQL("INSERT INTO metas_dia (nome, ordem) VALUES (?, ?)", arrayOf<Any>(nome, i + 1))
                }
            }
        }
    }
}
