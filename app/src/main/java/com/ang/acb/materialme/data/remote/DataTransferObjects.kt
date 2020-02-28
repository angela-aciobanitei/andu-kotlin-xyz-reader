package com.ang.acb.materialme.data.remote

import com.ang.acb.materialme.data.local.Article
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A Data Transfer Object representing a recipe step.
 */
@JsonClass(generateAdapter = true)
data class NetworkArticle(
    val id: Long,
    val body: String,
    val photo: String,
    val thumb: String,
    val author: String,
    val title: String,
    val aspect_ratio: Float,
    val published_date: String
)

/**
 * Converts the articles network response to an [Article] database object.
 */
fun NetworkArticle.asDatabaseArticle(): Article {
    return Article(
        id = id,
        body = body,
        photoUrl = photo,
        thumbUrl = thumb,
        author = author,
        title = title,
        aspectRatio = aspect_ratio,
        publishedDate = published_date
    )
}

