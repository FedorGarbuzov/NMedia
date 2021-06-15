package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewModel.SignInViewModel

@AndroidEntryPoint
class SignInFragment : DialogFragment() {
    private val viewModel: SignInViewModel by viewModels(
            ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentSignInBinding.inflate(layoutInflater, container, false)

        binding.enterButton.setOnClickListener {
            viewModel.updateUser(
                    binding.login.editText?.text.toString(),
                    binding.password.editText?.text.toString(),
            )
        }
        viewModel.authenticated.observe(viewLifecycleOwner) {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}