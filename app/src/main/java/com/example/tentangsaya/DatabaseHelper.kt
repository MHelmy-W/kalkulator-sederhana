package com.example.tentangsaya

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "UserDB", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE, " +
                    "password TEXT)"
        )

        // Membuat tabel notes
        db.execSQL(
            "CREATE TABLE notes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT NOT NULL, " +
                    "content TEXT NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS notes (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "content TEXT NOT NULL)"
            )
        }
    }

    fun insertUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=?", arrayOf(username))

        if (cursor.count > 0) {
            cursor.close()
            db.close()
            return false
        }
        cursor.close()

        val values = ContentValues().apply {
            put("username", username)
            put("password", password)
        }

        val result = db.insert("users", null, values)
        db.close()
        return result != -1L
    }

    fun checkUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, password))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun isUserExists(): Boolean {
        val db = readableDatabase
        var exists = false
        try {
            val cursor = db.rawQuery("SELECT * FROM users LIMIT 1", null)
            exists = cursor.count > 0
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return exists
    }

    // Fungsi untuk menambahkan catatan baru
    fun insertNote(title: String, content: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
        }
        val result = db.insert("notes", null, values)
        db.close()
        return result != -1L
    }

    // Fungsi untuk mengambil semua catatan
    fun getAllNotes(): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = readableDatabase
        var cursor = db.rawQuery("SELECT * FROM notes", null)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val note = Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("title")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content"))
                    )
                    notesList.add(note)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
            db.close()
        }

        return notesList
    }

    // Fungsi untuk memperbarui catatan
    fun updateNote(id: Int, title: String, content: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", title)
            put("content", content)
        }
        val result = db.update("notes", values, "id=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // Fungsi untuk menghapus catatan
    fun deleteNote(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("notes", "id=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}
