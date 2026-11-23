package com.example.motobitacora.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Base de datos SQLite local. Contiene las operaciones CRUD de la bitácora.
 * (Por ser una app pequeña, las consultas se hacen en el hilo principal.)
 */
class DbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TIPO TEXT NOT NULL,
                $COL_DESCRIPCION TEXT NOT NULL,
                $COL_FECHA TEXT NOT NULL,
                $COL_KM INTEGER NOT NULL,
                $COL_COSTO REAL NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versión 1: no hay migraciones todavía.
        db.execSQL("DROP TABLE IF EXISTS $TABLA")
        onCreate(db)
    }

    /** CREATE: devuelve el id generado (o -1 si falló). */
    fun insertar(registro: Registro): Long =
        writableDatabase.insert(TABLA, null, aValores(registro))

    /** READ: todos los registros, del más reciente al más antiguo. */
    fun obtenerTodos(): List<Registro> {
        val lista = mutableListOf<Registro>()
        readableDatabase.query(TABLA, null, null, null, null, null, "$COL_FECHA DESC, $COL_ID DESC")
            .use { cursor ->
                while (cursor.moveToNext()) lista.add(aRegistro(cursor))
            }
        return lista
    }

    /** READ: un registro por id, o null si no existe. */
    fun obtenerPorId(id: Long): Registro? {
        readableDatabase.query(TABLA, null, "$COL_ID = ?", arrayOf(id.toString()), null, null, null)
            .use { cursor ->
                return if (cursor.moveToFirst()) aRegistro(cursor) else null
            }
    }

    /** UPDATE: devuelve cuántas filas se modificaron. */
    fun actualizar(registro: Registro): Int =
        writableDatabase.update(TABLA, aValores(registro), "$COL_ID = ?", arrayOf(registro.id.toString()))

    /** DELETE: devuelve cuántas filas se borraron. */
    fun eliminar(id: Long): Int =
        writableDatabase.delete(TABLA, "$COL_ID = ?", arrayOf(id.toString()))

    private fun aValores(r: Registro) = ContentValues().apply {
        put(COL_TIPO, r.tipo)
        put(COL_DESCRIPCION, r.descripcion)
        put(COL_FECHA, r.fecha)
        put(COL_KM, r.km)
        put(COL_COSTO, r.costo)
    }

    private fun aRegistro(c: android.database.Cursor) = Registro(
        id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
        tipo = c.getString(c.getColumnIndexOrThrow(COL_TIPO)),
        descripcion = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPCION)),
        fecha = c.getString(c.getColumnIndexOrThrow(COL_FECHA)),
        km = c.getInt(c.getColumnIndexOrThrow(COL_KM)),
        costo = c.getDouble(c.getColumnIndexOrThrow(COL_COSTO))
    )

    companion object {
        private const val NOMBRE_BD = "motobitacora.db"
        private const val VERSION_BD = 1
        private const val TABLA = "registros"
        private const val COL_ID = "id"
        private const val COL_TIPO = "tipo"
        private const val COL_DESCRIPCION = "descripcion"
        private const val COL_FECHA = "fecha"
        private const val COL_KM = "km"
        private const val COL_COSTO = "costo"

        @Volatile
        private var instancia: DbHelper? = null

        /** Una sola instancia para toda la app. */
        fun obtener(context: Context): DbHelper =
            instancia ?: synchronized(this) {
                instancia ?: DbHelper(context.applicationContext).also { instancia = it }
            }
    }
}
