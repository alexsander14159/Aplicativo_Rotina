package com.rotina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Meta::class, ChecklistDia::class, Submeta::class, SubmetaDia::class, Dia::class],
    version = 2,
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
                .addMigrations(MIGRACAO_1_2)
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
