package com.example.stealthbrowser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
  @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
  fun getAllBookmarks(): Flow<List<BookmarkEntity>>

  @Query("SELECT * FROM bookmarks WHERE profileId IS NULL OR profileId = :profileId ORDER BY createdAt DESC")
  fun getBookmarksForProfile(profileId: String): Flow<List<BookmarkEntity>>

  @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
  suspend fun getBookmarkByUrl(url: String): BookmarkEntity?

  @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
  fun isUrlBookmarked(url: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity): Long

  @Delete
  suspend fun deleteBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM bookmarks WHERE id = :id")
  suspend fun deleteBookmarkById(id: Long)

  @Query("DELETE FROM bookmarks WHERE url = :url")
  suspend fun deleteBookmarkByUrl(url: String)

  @Query("DELETE FROM bookmarks WHERE profileId = :profileId")
  suspend fun deleteBookmarksByProfileId(profileId: String): Int

  @Query("DELETE FROM bookmarks WHERE profileId IN (:profileIds)")
  suspend fun deleteBookmarksByProfileIds(profileIds: List<String>): Int

  @Query("DELETE FROM bookmarks WHERE profileId IS NOT NULL AND profileId NOT IN (:validProfileIds)")
  suspend fun deleteOrphanedBookmarks(validProfileIds: List<String>): Int

  @Query("DELETE FROM bookmarks")
  suspend fun deleteAllBookmarks(): Int
}
