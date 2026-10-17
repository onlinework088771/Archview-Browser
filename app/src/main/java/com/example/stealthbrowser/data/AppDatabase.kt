package com.example.stealthbrowser.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
  abstract fun bookmarkDao(): BookmarkDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "stealth_browser_database"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              // Seed default testing & stealth verification bookmarks
              CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).bookmarkDao()
                dao.insertBookmark(BookmarkEntity(title = "Whoer IP & Leak Test", url = "https://whoer.net"))
                dao.insertBookmark(BookmarkEntity(title = "BrowserLeaks Suite", url = "https://browserleaks.com"))
                dao.insertBookmark(BookmarkEntity(title = "IPhey Anti-Fraud Score", url = "https://iphey.com"))
                dao.insertBookmark(BookmarkEntity(title = "Pixelscan Bot Detection", url = "https://pixelscan.net"))
                dao.insertBookmark(BookmarkEntity(title = "Google Search", url = "https://www.google.com"))
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
