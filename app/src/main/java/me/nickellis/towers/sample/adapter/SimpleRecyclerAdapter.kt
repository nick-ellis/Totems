package me.nickellis.towers.sample.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import me.nickellis.towers.sample.ktx.isValidIndexOf
import kotlin.math.min


abstract class SimpleRecyclerAdapter<T>(
  private val context: Context,
  @LayoutRes private val itemLayout: Int,
  items: Collection<T>
) : RecyclerView.Adapter<SimpleViewHolder>() {

  private val items: MutableList<T> = items.toMutableList()

  init {
    notifyDataSetChanged()
  }

  abstract fun onBind(item: T, holder: SimpleViewHolder, position: Int)

  fun remove(item: T): Boolean {
    return removeItemAt(items.indexOfFirst { it == item })
  }

  fun removeItemAt(index: Int): Boolean {
    if (index.isValidIndexOf(items)) {
      items.removeAt(index)
      notifyItemRemoved(index)
      return true
    }
    return false
  }

  fun add(item: T): SimpleRecyclerAdapter<T> {
    return addItemAt(items.size, item)
  }

  fun addItemAt(index: Int, item: T): SimpleRecyclerAdapter<T> {
    val adjusted = min(items.size, index)
    items.add(adjusted, item)
    notifyItemInserted(adjusted)
    return this
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder {
    val view = LayoutInflater.from(context).inflate(itemLayout, null, false)
    return SimpleViewHolder(view)
  }

  override fun getItemCount(): Int = items.size

  override fun onBindViewHolder(holder: SimpleViewHolder, position: Int) {
    onBind(items[position], holder, position)
  }
}

class SimpleViewHolder(v: View) : RecyclerView.ViewHolder(v)