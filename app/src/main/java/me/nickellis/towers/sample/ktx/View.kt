package me.nickellis.towers.sample.ktx

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

fun ViewGroup.children() = (0 until childCount).map { index -> getChildAt(index) }

fun <T : View> ViewGroup.addLayout(@LayoutRes id: Int): T {
  View.inflate(context, id, this)
  @Suppress("UNCHECKED_CAST")
  return getChildAt(childCount - 1) as T
}