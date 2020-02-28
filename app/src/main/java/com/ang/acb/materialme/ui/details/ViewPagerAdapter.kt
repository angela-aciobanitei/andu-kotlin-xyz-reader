package com.ang.acb.materialme.ui.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ang.acb.materialme.data.local.Article
import com.ang.acb.materialme.ui.details.ArticleDetailsFragment.Companion.newInstance

/**
 * FIXME: https://developer.android.com/training/animation/screen-slide
 */
class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    behavior: Int
) : FragmentStatePagerAdapter(fragmentManager, behavior) {
    private var articles: List<Article>? = null

    override fun getItem(position: Int): Fragment {
        return newInstance(articles!![position].id)
    }

    override fun getCount(): Int {
        return if (articles != null) articles!!.size else 0
    }

    fun submitList(articles: List<Article>?) {
        this.articles = articles
        notifyDataSetChanged()
    }
}