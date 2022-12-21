package com.app.testwebviewforlink.utils

import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.app.testwebviewforlink.R
import com.bumptech.glide.Glide

fun FragmentActivity.toast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}

fun Fragment.toast(str: String) {
    Toast.makeText(requireActivity(), str, Toast.LENGTH_SHORT).show()
}

fun ImageView.show(url: String) = Glide.with(this.context).load(url).placeholder(R.drawable.role3).into(this)