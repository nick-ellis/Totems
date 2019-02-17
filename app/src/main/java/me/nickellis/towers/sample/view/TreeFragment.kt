package me.nickellis.towers.sample.view

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.tree_fragment.*

import me.nickellis.towers.sample.R
import me.nickellis.towers.sample.repo.trees.ForestRepo
import me.nickellis.towers.sample.repo.trees.MockForest
import me.nickellis.towers.sample.view.model.TreeViewModel

class TreeFragment : BaseFragment() {

  companion object {
    fun newInstance(id: String) = TreeFragment().apply {
      arguments = (arguments ?: Bundle()).apply {
        putString("treeId", id)
      }
    }
  }

  private val forestRepo: ForestRepo = MockForest.getInstance()

  private lateinit var viewModel: TreeViewModel

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.tree_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(this).get(TreeViewModel::class.java)

    if (viewModel.tree == null) {
      arguments?.getString("treeId")?.let { id ->
        viewModel.tree = forestRepo.findTree(id)
      }
    }

    viewModel.tree?.apply {
      v_header.text = name
      v_subheader.text = id
    } ?: IllegalArgumentException("This tree is not of this forest")
  }

}
