package ru.netology.nmedia.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.ui.NewPostFragment.Companion.postArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentCardBinding
import ru.netology.nmedia.dto.AttachmentType
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
                    if (it.attachment?.url == null) attachment.visibility = View.GONE
                    postMenu.visibility = if (it.ownedByMe) View.VISIBLE else View.INVISIBLE

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

                    done?.isVisible = it.ownedByMe
                    done.isEnabled = it.uploadedToServer

                    if (!done.isEnabled) {
                        favorite.isClickable = false
                        share.isClickable = false
                    }

                    val url = "$BASE_URL/avatars/${it.authorAvatar}"
                    Glide.with(binding.postAvatar)
                        .load(url)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .circleCrop()
                        .into(binding.postAvatar)

                    val attUrl = "$BASE_URL/images/${it.attachment?.url}"
                    Glide.with(binding.attachment)
                        .load(attUrl)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(binding.attachment)

                    val myUrl = "$BASE_URL/media/${it.attachment?.url}"
                    Glide.with(binding.attachment)
                        .load(myUrl)
                        .placeholder(R.drawable.ic_loading_100dp)
                        .error(R.drawable.ic_error_100dp)
                        .timeout(10_000)
                        .into(binding.attachment)
                }

                binding.attachment.setOnClickListener { _ ->
                    when (it?.attachment?.type) {
                        AttachmentType.IMAGE -> findNavController().navigate(
                            R.id.action_cardFragment_to_imageFragment,
                            Bundle().apply {
                                textArg = it.attachment.url
                            })
                        else -> {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it?.attachment?.url))
                            if (activity?.packageManager?.let { intent.resolveActivity(it) } != null) {
                                startActivity(intent)
                            } else {
                                Toast.makeText(activity, R.string.app_not_found, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        return binding.root
    }
}