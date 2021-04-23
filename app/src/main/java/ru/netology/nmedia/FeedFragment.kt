package ru.netology.nmedia

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
//import ru.netology.nmedia.NewPostFragment.Companion.postArg
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInterractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.enum.AttachmentType
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.viewModel.PostViewModel

class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
    )

    val Fragment.packageManager: PackageManager?
        get() = context?.packageManager

    @SuppressLint("ResourceType", "ShowToast", "SetTextI18n")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {

        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnInterractionListener {
            override fun onLike(post: Post) {
                if (!post.likedByMe) viewModel.likedByMe(post.id) else viewModel.unlikedByMe(post.id)
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
//                                postArg = post
                            })
                    else -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                        if (packageManager?.let { intent.resolveActivity(it) } != null) {
                            startActivity(intent)
                        } else {
                            Toast.makeText(activity, R.string.app_not_found, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onOpenCard(post: Post) {
                findNavController().navigate(
                        R.id.action_feedFragment_to_cardFragment,
                        Bundle().apply {
//                            postArg = post
                        })
            }
        })

        binding.postsList.adapter = adapter
        viewModel.dataState.observe(viewLifecycleOwner,
                { state ->
                    binding.progress.isVisible = state.loading
                    binding.errorLoadingGroup.isVisible = state.errorLoading
                    binding.errorSavingGroup.isVisible = state.errorSaving
                    binding.swipeRefresh.isRefreshing = state.refreshing
                })

        viewModel.data.observe(viewLifecycleOwner,
                { state ->
                    adapter.submitList(state.posts)
                    binding.emptyText.isVisible = state.empty
                })

        viewModel.getNewer.observe(viewLifecycleOwner) { state ->
            println(state)
            if (state.isNotEmpty()) {
                binding.newer.visibility = View.VISIBLE
                binding.newer.setOnClickListener {
                    viewModel.loadNewer()
                    binding.newer.visibility = View.GONE
                }
            }
        }

        adapter.registerAdapterDataObserver( object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
            ) {
                binding.postsList.scrollToPosition(0)
            }
        })


        binding.retryLoadingButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.retrySavingButton.setOnClickListener {
            viewModel.save()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}