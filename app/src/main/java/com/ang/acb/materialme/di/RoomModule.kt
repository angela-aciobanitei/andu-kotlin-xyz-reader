package com.ang.acb.materialme.di

import android.app.Application
import androidx.room.Room
import com.ang.acb.materialme.data.local.AppDatabase
import com.ang.acb.materialme.data.local.ArticleDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * A Dagger module that provides the Room database for this app, and the [MovieDao].
 */
@Module(includes = [ViewModelModule::class])
class RoomModule {
    @Singleton
    @Provides
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "articles.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideArticleDao(database: AppDatabase): ArticleDao {
        return database.articlesDao()
    }
}