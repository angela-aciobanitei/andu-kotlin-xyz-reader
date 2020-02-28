package com.ang.acb.materialme.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Interface for database access on [Article] related operations.
 */
@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertArticles(articles: List<Article>)

    @Query("SELECT * FROM article WHERE id = :id")
    fun getArticleById(id: Long): LiveData<Article>

    @Transaction
    @Query("SELECT * FROM article")
    fun getAllArticles(): LiveData<List<Article>>
}

/**
 * The [RoomDatabase] for this app.
 */
@Database(entities = [Article::class], exportSchema = false, version = 1)
abstract class AppDatabase(): RoomDatabase() {
    abstract fun articlesDao(): ArticleDao
}