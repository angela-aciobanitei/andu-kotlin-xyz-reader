package com.ang.acb.materialme.data.remote

import com.ang.acb.materialme.data.local.Article
import com.squareup.moshi.JsonClass

/**
 * A Data Transfer Object representing a recipe step.
 */
@JsonClass(generateAdapter = true)
data class NetworkArticle(
    val id: Long,
    val body: String,
    val photoUrl: String,
    val thumbUrl: String,
    val author: String,
    val title: String,
    val aspectRatio: Float,
    val publishedDate: String
)

/**
 * Converts the articles network response to an [Article] database object.
 */
fun NetworkArticle.asDatabaseArticle(): Article {
    return Article(
        id = id,
        body = body,
        photoUrl = photoUrl,
        thumbUrl = thumbUrl,
        author = author,
        title = title,
        aspectRatio = aspectRatio,
        publishedDate = publishedDate
    )
}

