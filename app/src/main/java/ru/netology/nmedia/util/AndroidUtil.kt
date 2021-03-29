package ru.netology.nmedia.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun display(num: Int): String {
        var newString = "$num"
        if (num >= 1_000) {
            val first = Character.getNumericValue(num.toString()[0])
            val second = Character.getNumericValue(num.toString()[1])
            val third = Character.getNumericValue(num.toString()[2])
            when {
                (num in 1_000..9_999) -> newString = "${first}.${second}K"
                (num in 10_000..99_999) -> newString = "${first}${second}K"
                (num in 100_000..999_999) -> newString = "${first}${second}${third}K"
                (num in 1_000_000..9_999_999) -> newString = "${first}.${second}M"
                (num in 10_000_000..99_999_999) -> newString = "${first}${second}.${third}M"
                (num in 100_000_000..999_999_000) -> newString = "${first}${second}${third}M"
            }
        }
        return newString
    }
}