package me.nickellis.towers.sample.adapter

import android.content.Context
import android.widget.TextView
import me.nickellis.towers.sample.R
import me.nickellis.towers.sample.data.Tree


class ForestAdapter(
  context: Context,
  forest: Collection<Tree>
) : SimpleRecyclerAdapter<Tree>(context, R.layout.item_tree, forest) {

  override fun onBind(item: Tree, holder: SimpleViewHolder, position: Int) {
    holder.itemView.apply {
      findViewById<TextView>(R.id.v_tree_header).text = item.name
      findViewById<TextView>(R.id.v_tree_subheader).text = item.id

      setOnClickListener { onTreePicked?.invoke(item) }
    }
  }

  private var onTreePicked: ((tree: Tree) -> Unit)? = null
  fun onTreePicked(l: (tree: Tree) -> Unit): ForestAdapter {
    onTreePicked = l
    return this
  }

}