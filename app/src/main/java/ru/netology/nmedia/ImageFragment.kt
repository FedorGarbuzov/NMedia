package ru.netology.nmedia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ru.netology.nmedia.NewPostFragment.Companion.postArg
import ru.netology.nmedia.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.util.AndroidUtils.display

class ImageFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentImageBinding.inflate(inflater, container, false)

        val url = "http://10.0.2.2:9999/images/${arguments?.textArg}"
        Glide.with(binding.image)
                .load(url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.image)

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