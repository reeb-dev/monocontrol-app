package com.manuelreeb.monocontrol.data.local

import android.content.Context
import androidx.room.*
import com.manuelreeb.monocontrol.data.local.entity.AlertaEntity
import com.manuelreeb.monocontrol.data.local.entity.ClienteEntity
import com.manuelreeb.monocontrol.data.local.entity.MovimientoEntity
import com.manuelreeb.monocontrol.data.local.entity.PerfilEntity
import com.manuelreeb.monocontrol.domain.model.TipoMovimiento

@Database(
    entities = [
        MovimientoEntity::class,
        AlertaEntity::class,
        PerfilEntity::class,
        ClienteEntity::class
    ],
    version = 13,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao
    abstract fun alertaDao(): AlertaDao
    abstract fun perfilDao(): PerfilDao
    abstract fun clienteDao(): ClienteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monotributo_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

class Converters {
    @TypeConverter
    fun fromTipoMovimiento(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun toTipoMovimiento(value: String): TipoMovimiento = TipoMovimiento.valueOf(value)
}
