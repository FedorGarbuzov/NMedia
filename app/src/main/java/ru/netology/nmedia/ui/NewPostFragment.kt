package ru.netology.nmedia.ui

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.loadImage
import ru.netology.nmedia.viewModel.AuthViewModel
import ru.netology.nmedia.viewModel.PostViewModel
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        private const val TEXT_KEY = "TEXT_KEY"
        var Bundle.textArg: String?
            set(value) = putString(TEXT_KEY, value)
            get() = getString(TEXT_KEY)

        private const val POST_KEY = "POST_KEY"
        var Bundle.postArg: Post?
            set(value) = putParcelable(POST_KEY, value)
            get() = getParcelable(POST_KEY)
    }

    private val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment
    )
    @Inject
    lateinit var auth: AppAuth
    private val authViewModel: AuthViewModel by viewModels()

    private var fragmentBinding: FragmentNewPostBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_post_options, menu)
        inflater.inflate(R.menu.menu, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !authViewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, authViewModel.authenticated)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {
                fragmentBinding?.let { binding ->
                    viewModel.changeContent(binding.edit.text.toString())
                    viewModel.save()
                    AndroidUtils.hideKeyboard(requireView())
                }
                true
            }
            R.id.signin -> {
                SignInFragment().show(parentFragmentManager, "Dialog")
                true
            }
            R.id.signout -> {
                val dialog = activity?.let { activity ->
                    AlertDialog.Builder(activity)
                }

                dialog
                        ?.setMessage(R.string.cancelation)
                        ?.setPositiveButton(R.string.dialog_positive) { dialog, int ->
                            auth.removeAuth()
                            dialog.dismiss()
                            findNavController().navigateUp()

                        }
                        ?.setNegativeButton(R.string.dialog_negative) { dialog, int ->
                            dialog.cancel()
                        }
                        ?.create()
                        ?.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)

        fragmentBinding = binding

        arguments?.postArg
                ?.let { post ->
                    binding.edit.setText(post.content)
                    val mediaUrl = "${BASE_URL}/media/${post.attachment?.url}"
                    loadImage(binding.photo, mediaUrl)
                    viewModel.changePhoto(post.attachment?.url?.toUri())
                }

        binding.edit.requestFocus()

        val pickPhotoLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    when (it.resultCode) {
                        ImagePicker.RESULT_ERROR -> {
                            Snackbar.make(
                                    binding.root,
                                    ImagePicker.getError(it.data),
                                    Snackbar.LENGTH_LONG
                            ).show()
                        }
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.data
                            viewModel.changePhoto(uri)
                        }
                    }
                }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .galleryOnly()
                    .galleryMimeTypes(arrayOf("image/*"))
                    .createIntent(pickPhotoLauncher::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .cameraOnly()
                    .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}