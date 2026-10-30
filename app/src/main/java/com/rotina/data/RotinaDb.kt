package com.rotina.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Meta::class, ChecklistDia::class],
    version = 1,
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
                .build()

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
