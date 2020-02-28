package com.ang.acb.materialme.ui.details

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.ang.acb.materialme.R
import com.ang.acb.materialme.ui.grid.ArticleGridFragment
import com.ang.acb.materialme.ui.viewmodel.ArticlesViewModel
import com.ang.acb.materialme.util.autoCleared
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * A fragment for displaying a [ViewPager] containing a series of article details.
 *
 * See: https://github.com/android/animation-samples/tree/master/GridToPager
 * See: https://android-developers.googleblog.com/2018/02/continuous-shared-element-transitions.html
 */
class ArticlesPagerFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // Note: multiple fragments can share a ViewModel using their activity scope.
    // https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
    private val viewModel: ArticlesViewModel by activityViewModels { viewModelFactory }

    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var articlesViewPager: ViewPager

    override fun onAttach(context: Context) {
        // When using Dagger with Fragments, inject as early as possible.
        // This prevents inconsistencies if the Fragment is reattached.
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Avoid a postponeEnterTransition() on orientation change,
        // and postpone only on first creation.
        if (savedInstanceState == null) postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_articles_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager(view)
        populateUi()
        prepareTransitions()
    }

    private fun setupViewPager(view: View) {
        // Because ArticlesPagerFragment contains a series of article details fragments
        // we need to initialize the view pager adapter with the child fragment manager.
        viewPagerAdapter = ViewPagerAdapter(
            childFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        )
        articlesViewPager = view.findViewById(R.id.articles_view_pager)
        articlesViewPager.adapter = viewPagerAdapter
        articlesViewPager.addOnPageChangeListener(
            object : SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    viewModel.position = position
                }
            })
    }

    private fun populateUi() {
        viewModel.articles.observe(viewLifecycleOwner, Observer { resource ->
                if (resource?.data != null) {
                    // Update data in the view pager adapter.
                    viewPagerAdapter.submitList(resource.data)
                    // Set the currently selected page for the view pager.
                    // To transition immediately, set smoothScroll to false.
                    articlesViewPager.setCurrentItem(
                        viewModel.position, false
                    )
                }
            }
        )
    }

    /**
     * Prepares the shared element transition from and back to the [ArticleGridFragment].
     */
    private fun prepareTransitions() {
        val enterTransition = TransitionInflater.from(context)
                .inflateTransition(R.transition.image_view_enter_transition)
        enterTransition.duration = 375
        sharedElementEnterTransition = enterTransition
        // We would like to support a seamless back and forth transition. This includes
        // a transition from the grid to the pager, and then a transition back to the
        // relevant image, even when the user paged to a different image. To do so, we
        // will need to find a way to dynamically remap the shared elements. To do this,
        // first, we'll set a transition name on the image views by calling setTransitionName.
        // Then  we'll set up SharedElementCallbacks to intercept onMapSharedElements() and
        // adjust the mapping of the shared element names to views. This will be done when
        // exiting the ArticleGridFragment and when entering the ArticlesPagerFragment.
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                sharedElements: MutableMap<String, View>
            ) {
                // Locate the image view at the primary fragment (the ArticleDetailsFragment
                // that is currently visible). To locate the fragment, call instantiateItem()
                // with the current position. At this stage, the method will simply return
                // the fragment at the position and will not create a new one.
                val currentFragment = viewPagerAdapter.instantiateItem(
                        articlesViewPager, viewModel.position
                    ) as Fragment
                // Get the root view for the current fragment layout.
                val rootView = currentFragment.view ?: return

                // We are only interested in a single ImageView transition from the grid to the
                // fragment the view-pager holds, so the mapping only needs to be adjusted for
                // the first named element received at the onMapSharedElements() callback.
                val transitioningView =
                    rootView.findViewById<ImageView>(R.id.details_article_photo)
                sharedElements[names[0]] = transitioningView
            }
        })
    }
}
