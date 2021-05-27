package ru.netology.nmedia.ui

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.databinding.FragmentSignUpBinding
import ru.netology.nmedia.viewModel.SignUpViewModel

class SignUpFragment : DialogFragment() {
    private val viewModel: SignUpViewModel by viewModels(
            ownerProducer = ::requireParentFragment
    )

    private var fragmentBinding: FragmentNewPostBinding? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        binding.registerButton.setOnClickListener {
            if (binding.password.editText?.text.toString() == binding.confirmPassword.editText?.text.toString()) {
                viewModel.createUser(
                        binding.login.editText?.text.toString(),
                        binding.password.editText?.text.toString(),
                        binding.name.editText?.text.toString()
                )
            } else {
                Toast.makeText(activity, R.string.fields_are_not_the_same, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.registered.observe(viewLifecycleOwner) {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

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

        binding.setAvatar.setOnClickListener {
            ImagePicker.with(this)
                    .crop()
                    .compress(2048)
                    .galleryOnly()
                    .galleryMimeTypes(arrayOf("image/*"))
                    .createIntent(pickPhotoLauncher::launch)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = resources.displayMetrics.widthPixels
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}