package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.ui.NewPostFragment.Companion.postArg
import ru.netology.nmedia.ui.NewPostFragment.Companion.textArg
import ru.netology.nmedia.util.AndroidUtils.display
import ru.netology.nmedia.util.AndroidUtils.loadImage

class ImageFragment : Fragment() {

    @ExperimentalCoroutinesApi
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentImageBinding.inflate(inflater, container, false)

        val mediaUrl = "$BASE_URL/media/${arguments?.textArg}"
        loadImage(binding.image, mediaUrl)

        arguments?.postArg?.let {
            binding.apply {
                buttonLikes.text = display(it.likes)
                buttonShare.text = display(it.share)
                buttonViews.text = display(it.views)
                if (it.likedByMe) toggleButton.check(buttonLikes.id)
            }
        }

        binding.topAppBar.setOnMenuItemClickListener {
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return binding.root
    }
}