package com.ang.acb.materialme.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Immutable model class for an article.
 */
@Entity(tableName = "article")
data class Article(
    @PrimaryKey
    val id: Long,
    val body: String,
    @ColumnInfo(name = "photo_url")
    val photoUrl: String,
    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String,
    val author: String,
    val title: String,
    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Double,
    @ColumnInfo(name = "published_date")
    val publishedDate: String
)