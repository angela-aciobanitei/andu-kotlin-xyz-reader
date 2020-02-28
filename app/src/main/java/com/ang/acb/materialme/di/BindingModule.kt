package com.ang.acb.materialme.di

import com.ang.acb.materialme.ui.common.MainActivity
import com.ang.acb.materialme.ui.details.ArticleDetailsFragment
import com.ang.acb.materialme.ui.details.ArticlesPagerFragment
import com.ang.acb.materialme.ui.grid.ArticleGridFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * A Dagger module for adding the [MainActivity] to the dependency graph.
 */
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentsMainModule::class])
    abstract fun contributeMainActivity(): MainActivity
}

/**
 * A Dagger module for the fragments hosted by the [MainActivity]
 */
@Module
abstract class FragmentsMainModule {
    @ContributesAndroidInjector
    abstract fun contributeArticleListFragment(): ArticleGridFragment?

    @ContributesAndroidInjector
    abstract fun contributeArticlesViewPagerFragment(): ArticlesPagerFragment?

    @ContributesAndroidInjector
    abstract fun contributeArticleDetailsFragment(): ArticleDetailsFragment?
}