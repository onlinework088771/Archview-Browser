package com.example.stealthbrowser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val url: String,
  val profileId: String? = null, // null means global/shared bookmark
  val createdAt: Long = System.currentTimeMillis()
)
