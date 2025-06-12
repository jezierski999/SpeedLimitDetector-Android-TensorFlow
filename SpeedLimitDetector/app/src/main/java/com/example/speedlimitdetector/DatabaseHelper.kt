package com.example.speedlimitdetector

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SLD"
        const val TABLE_NAME = "Settings"
        const val COLUMN_ID = "id"
        const val COLUMN_VALUE = "value"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val dropTable = "DROP TABLE IF EXISTS $TABLE_NAME"
        db.execSQL(dropTable)
        db?.execSQL("CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_VALUE REAL DEFAULT 0.0)")
        for (i in 1..5) {
            val contentValues = ContentValues()
            contentValues.put(COLUMN_VALUE, 0.0)
            db?.insert(TABLE_NAME, null, contentValues)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropTable = "DROP TABLE IF EXISTS $TABLE_NAME"
        db.execSQL(dropTable)
        onCreate(db)
    }

    fun updateValue(id: Int, newValue: Float) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_VALUE, newValue)
        db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun getValueById(id: Int): Float {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_VALUE FROM $TABLE_NAME WHERE $COLUMN_ID = $id"
        val cursor = db.rawQuery(query, null)
        var value: Float = 0.0f
        if (cursor.moveToFirst()) {
            value = cursor.getFloat(0)
        }
        cursor.close()
        db.close()
        return value
    }
}
