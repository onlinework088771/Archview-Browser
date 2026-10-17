package com.example.stealthbrowser.data

import kotlinx.coroutines.flow.Flow

class BookmarkRepository(private val bookmarkDao: BookmarkDao) {
  val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

  fun getBookmarksForProfile(profileId: String): Flow<List<BookmarkEntity>> =
    bookmarkDao.getBookmarksForProfile(profileId)

  suspend fun isBookmarked(url: String): Boolean =
    bookmarkDao.getBookmarkByUrl(url) != null

  fun isUrlBookmarkedFlow(url: String): Flow<Boolean> =
    bookmarkDao.isUrlBookmarked(url)

  suspend fun addBookmark(title: String, url: String, profileId: String? = null): Long {
    val cleanUrl = url.trim()
    val cleanTitle = title.trim().ifBlank { cleanUrl }
    return bookmarkDao.insertBookmark(
      BookmarkEntity(
        title = cleanTitle,
        url = cleanUrl,
        profileId = profileId
      )
    )
  }

  suspend fun deleteBookmark(bookmark: BookmarkEntity) =
    bookmarkDao.deleteBookmark(bookmark)

  suspend fun deleteBookmarkById(id: Long) =
    bookmarkDao.deleteBookmarkById(id)

  suspend fun deleteBookmarkByUrl(url: String) =
    bookmarkDao.deleteBookmarkByUrl(url)

  suspend fun deleteBookmarksByProfileId(profileId: String): Int =
    bookmarkDao.deleteBookmarksByProfileId(profileId)

  suspend fun deleteBookmarksByProfileIds(profileIds: List<String>): Int =
    bookmarkDao.deleteBookmarksByProfileIds(profileIds)

  suspend fun deleteOrphanedBookmarks(validProfileIds: List<String>): Int =
    bookmarkDao.deleteOrphanedBookmarks(validProfileIds)

  suspend fun deleteAllBookmarks(): Int =
    bookmarkDao.deleteAllBookmarks()
}
