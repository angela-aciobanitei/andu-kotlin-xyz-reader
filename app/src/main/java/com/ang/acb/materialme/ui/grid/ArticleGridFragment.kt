package com.ang.acb.materialme.ui.grid

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.ang.acb.materialme.R
import com.ang.acb.materialme.databinding.FragmentArticleGridBinding
import com.ang.acb.materialme.ui.details.ArticlesPagerFragment
import com.ang.acb.materialme.ui.viewmodel.ArticlesViewModel
import com.ang.acb.materialme.util.autoCleared
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


/**
 * A [Fragment] that displays a list of articles as grids.
 */
class ArticleGridFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // Note: multiple fragments can share a ViewModel using their activity scope.
    // https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
    private val sharedViewModel: ArticlesViewModel by activityViewModels { viewModelFactory }

    private var binding: FragmentArticleGridBinding by autoCleared()
    private var adapter: ArticlesAdapter by autoCleared()

    private lateinit var isEnterTransitionStarted: AtomicBoolean

    override fun onAttach(context: Context) {
        // When using Dagger with Fragments, inject as early as possible.
        // This prevents inconsistencies if the Fragment is reattached.
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for this fragment.
        binding = FragmentArticleGridBinding.inflate(inflater)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        prepareTransitions()
        setupRecyclerView()
        setupAdapter()
        populateUi()
        scrollToPosition()
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(binding.gridsToolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupRecyclerView() {
        binding.articlesRecyclerView.layoutManager = StaggeredGridLayoutManager(
            resources.getInteger(R.integer.grid_column_count),
            StaggeredGridLayoutManager.VERTICAL
        )
    }

    /**
     * Prepares the shared element transition to and from the [ArticlesPagerFragment].
     */
    private fun prepareTransitions() {
        isEnterTransitionStarted = AtomicBoolean()
        // To make transitions even smoother, we could fade out the
        // grid items when the image transitions to the view pager.
        val exitTransition = TransitionInflater.from(context)
                .inflateTransition(R.transition.card_view_exit_transition)
        exitTransition.duration = 325
        setExitTransition(exitTransition)
        // We would like to support a seamless back and forth transitions. This includes
        // a transition from the grid to the pager, and then a transition back to the
        // relevant image, even when the user paged to a different image. To do so, we
        // will need to find a way to dynamically remap the shared elements. To do this,
        // first, we'll set a transition name on the image views by calling setTransitionName.
        // Then  we'll set up SharedElementCallbacks to intercept onMapSharedElements() and
        // adjust the mapping of the shared element names to views. This will be done when
        // exiting the ArticleGridFragment and when entering the ArticlesPagerFragment.
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                sharedElements: MutableMap<String, View>
            ) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder = binding.articlesRecyclerView
                    .findViewHolderForAdapterPosition(sharedViewModel.currentPosition)
                if (selectedViewHolder?.itemView == null) return
                // We are only interested in a single ImageView transition from the grid to the
                // fragment the view pager holds, so the mapping only needs to be adjusted for
                // the first named element received at the onMapSharedElements() callback.
                val transitioningView = selectedViewHolder.itemView
                    .findViewById<ImageView>(R.id.article_item_thumbnail)
                sharedElements[names[0]] = transitioningView
            }
        })
    }

    private fun setupAdapter() {
        adapter = ArticlesAdapter(
            itemClickListener = { view, position -> handleItemClicks(view, position)},
            imageLoadListener = { position -> schedulePostponedEnterTransition(position) }
        )
        binding.articlesRecyclerView.adapter = adapter
    }

    private fun handleItemClicks(rootView: View, position: Int) {
        // Save current position to view model.
        sharedViewModel.currentPosition = position

        // Exclude the clicked card from the exit transition.
        (exitTransition as TransitionSet).excludeTarget(rootView, true)

        // Create the shared element transition extras.
        val transitioningView: ImageView = rootView.findViewById(R.id.article_item_thumbnail)
        val extras = FragmentNavigator.Extras.Builder()
            .addSharedElement(transitioningView, transitioningView.transitionName)
            .build()

        // Navigate to destination, passing in the shared element as extras.
        NavHostFragment.findNavController(this).navigate(
            R.id.action_from_articles_grid_to_articles_pager,
            null, null, extras
        )
    }

    private fun schedulePostponedEnterTransition(position: Int) {
        if (sharedViewModel.currentPosition != position) return
        if (isEnterTransitionStarted.getAndSet(true)) return
        // Before calling startPostponedEnterTransition(), make sure that the view is drawn.
        binding.articlesRecyclerView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    binding.articlesRecyclerView.viewTreeObserver
                        .removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }
            }
        )
    }

    private fun populateUi() {
        sharedViewModel.articles.observe(viewLifecycleOwner, Observer { resource ->
            // Note: after calling postponeEnterTransition(), don't forget
            // to call startPostponedEnterTransition(). Forgetting to do so
            // will leave your application in a state of deadlock, preventing
            // the user from ever being able to reach the next screen.
            postponeEnterTransition()
            binding.resource = resource
            if (resource?.data != null) {
                adapter.submitList(resource.data)
            }
        })
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid.
     * This is important when navigating back from the grid.
     */
    private fun scrollToPosition() {
        binding.articlesRecyclerView.addOnLayoutChangeListener(object :
            View.OnLayoutChangeListener {
            override fun onLayoutChange(
                view: View, left: Int, top: Int, right: Int, bottom: Int,
                oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                binding.articlesRecyclerView.removeOnLayoutChangeListener(this)
                val layoutManager =
                    binding.articlesRecyclerView.layoutManager
                val viewAtPosition =
                    layoutManager?.findViewByPosition(sharedViewModel.currentPosition)
                // Scroll to position if the view for the current position is null (not
                // currently part of layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(
                        viewAtPosition, false, true
                    )
                ) {
                    binding.articlesRecyclerView.post {
                        layoutManager?.scrollToPosition(sharedViewModel.currentPosition)
                    }
                }
            }
        })
    }
}
