package com.ang.acb.materialme.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ang.acb.materialme.ui.viewmodel.ArticlesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * A Dagger module that provides the view models for this app and a Dagger2-backed
 * factory for creating them.
 *
 * Note: Dagger allows binding several objects into a collection even when the objects
 * are bound in different modules using multibindings. Here, we use a Map multibinding.
 *
 * See: https://dagger.dev/multibindings.html
 */
@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(ArticlesViewModel::class)
    abstract fun bindArticlesViewModel(articlesViewModel: ArticlesViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}