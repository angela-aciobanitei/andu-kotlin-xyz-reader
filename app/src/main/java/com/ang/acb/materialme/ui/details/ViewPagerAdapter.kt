package com.ang.acb.materialme.ui.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ang.acb.materialme.data.local.Article

/**
 * A simple pager adapter that manages each page in the articles ViewPager.
 */
class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    behavior: Int
) : FragmentStatePagerAdapter(fragmentManager, behavior) {
    private var articles: List<Article>? = null

    override fun getItem(position: Int): Fragment {
        return ArticleDetailsFragment.newInstance(articles!![position].id)
    }

    override fun getCount(): Int {
        return articles?.size ?: 0
    }

    fun submitList(articles: List<Article>?) {
        this.articles = articles
        notifyDataSetChanged()
    }
}