package me.nickellis.towers.sample.view

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.forest_fragment.*

import me.nickellis.towers.sample.R
import me.nickellis.towers.sample.adapter.ForestAdapter
import me.nickellis.towers.sample.data.Tree
import me.nickellis.towers.sample.repo.trees.ForestRepo
import me.nickellis.towers.sample.repo.trees.MockForest
import me.nickellis.towers.sample.view.model.ForestViewModel

class ForestFragment : BaseFragment() {

  companion object {
    fun newInstance() = ForestFragment()
  }

  interface Listener {
    fun newTreePicked(tree: Tree)
  }

  private val forestRepo: ForestRepo = MockForest.getInstance()
  private lateinit var viewModel: ForestViewModel
  private var listener: Listener? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.forest_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    listener = (activity as? Listener)

    viewModel = ViewModelProviders.of(this).get(ForestViewModel::class.java)

    if (viewModel.forest.isEmpty()) {
      viewModel.forest.addAll(forestRepo.findTrees())
    }

    activity?.let { activity ->
      v_recycler.apply {
        layoutManager = LinearLayoutManager(activity)
        adapter = ForestAdapter(activity, viewModel.forest)
          .onTreePicked { listener?.newTreePicked(it) }
      }
    }
  }
}
