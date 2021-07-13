package ru.netology.nmedia.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.FeedAdapter
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.NewPostFragment.Companion.postArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.viewModel.AuthViewModel
import ru.netology.nmedia.viewModel.PostViewModel
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    @Inject
    lateinit var auth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()

    val Fragment.packageManager: PackageManager?
        get() = context?.packageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                SignInFragment().show(parentFragmentManager, "Dialog")
                true
            }
            R.id.signout -> {
                auth.removeAuth()
                true
            }
            R.id.signup -> {
                SignUpFragment().show(parentFragmentManager, "Dialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("ResourceType", "ShowToast", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = FeedAdapter(object : FeedAdapter.OnInteractionListener {
            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    if (!post.likedByMe) viewModel.likedByMe(post.id) else viewModel.unlikedByMe(
                        post.id
                    )
                } else {
                    SignInFragment().show(parentFragmentManager, "Dialog")
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plane"
                }.let { Intent.createChooser(it, null) }

                startActivity(intent)
                viewModel.shareById(post.id)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                        postArg = post
                        viewModel.edit(post)
                    })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPlayMedia(post: Post) {
                when (post.attachment?.type) {
                    AttachmentType.IMAGE -> findNavController().navigate(
                        R.id.action_feedFragment_to_imageFragment,
                        Bundle().apply {
                            textArg = post.attachment.url
                            postArg = post
                        })
                    else -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                        if (packageManager?.let { intent.resolveActivity(it) } != null) {
                            startActivity(intent)
                        } else {
                            Toast.makeText(activity, R.string.app_not_found, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }

            override fun onOpenCard(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_cardFragment,
                    Bundle().apply {
                        postArg = post
                    })
            }
        })

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        binding.postsList.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                    scrollUp(adapter, binding)
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        viewModel.dataState.observe(viewLifecycleOwner,
            { state ->
                binding.progress.isVisible = state.loading
                binding.errorLoadingGroup.isVisible = state.errorLoading
                binding.errorSavingGroup.isVisible = state.errorSaving
                binding.swipeRefresh.isRefreshing = state.refreshing
            })

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading
            }
        }

        authViewModel.data.observe(viewLifecycleOwner) {
            activity?.invalidateOptionsMenu()
        }

        viewModel.getNewer.observe(viewLifecycleOwner) { state ->
            if (state.isNotEmpty()) {
                binding.newer.visibility = View.VISIBLE
                binding.newer.setOnClickListener {
                    viewModel.loadNewer()
                    binding.newer.visibility = View.GONE
                }
            }
        }

        binding.retryLoadingButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.retrySavingButton.setOnClickListener {
            viewModel.save()
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
            binding.swipeRefresh.isRefreshing = false
            scrollUp(adapter, binding)
        }

        binding.add.setOnClickListener {
            if (authViewModel.authenticated) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                SignInFragment().show(parentFragmentManager, "Dialog")
            }
        }

        return binding.root

    }

    private fun scrollUp(adapter: FeedAdapter, binding: FragmentFeedBinding) {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.postsList.scrollToPosition(0)
            }
        })
    }
}