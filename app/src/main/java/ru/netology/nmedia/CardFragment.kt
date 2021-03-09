package ru.netology.nmedia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.NewPostFragment.Companion.postArg
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentCardBinding
import ru.netology.nmedia.util.AndroidUtils.display
import ru.netology.nmedia.viewModel.PostViewModel

class CardFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentCardBinding.inflate(inflater, container, false)

        arguments?.postArg.let {
            binding.apply {
                if (it != null) {
                    userName.text = it.author
                    published.text = it.published
                    content.text = it.content
                    share.text = display(it.share)
                    favorite.text = display(it.likes)
                    views.text = display(it.views)
                    video.text = it.url
                    if (it.url == null) video.visibility = View.GONE

                    favorite.isChecked = it.likedByMe

                    postMenu.setOnClickListener { view ->
                        PopupMenu(view.context, view).apply {
                            inflate(R.menu.post_options)
                            setOnMenuItemClickListener { item ->
                                when (item.itemId) {
                                    R.id.remove -> {
                                        viewModel.removeById(it.id)
                                        findNavController().navigateUp()
                                        true
                                    }
                                    R.id.edit -> {
                                        findNavController().navigate(
                                                R.id.action_cardFragment_to_newPostFragment,
                                                Bundle().apply {
                                                    textArg = it.content
                                                    viewModel.edit(it)
                                                })
                                        true
                                    }
                                    else -> false
                                }
                            }
                        }.show()
                    }
                    video.setOnClickListener { _ ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url))
                        startActivity(intent)
                    }

                    val url = "http://10.0.2.2:9999/avatars/${it.authorAvatar}"
                    Glide.with(binding.postAvatar)
                            .load(url)
                            .placeholder(R.drawable.ic_loading_100dp)
                            .error(R.drawable.ic_error_100dp)
                            .timeout(10_000)
                            .circleCrop()
                            .into(binding.postAvatar)
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        return binding.root
    }
}
