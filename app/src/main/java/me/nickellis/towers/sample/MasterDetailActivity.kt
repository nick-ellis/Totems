package me.nickellis.towers.sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import me.nickellis.towers.Towers
import me.nickellis.towers.sample.data.Tree
import me.nickellis.towers.sample.view.ForestFragment
import me.nickellis.towers.sample.view.TreeFragment

class MasterDetailActivity : BaseActivity(), Towers.Listener, ForestFragment.Listener {

  private lateinit var towers: Towers

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_master_detail)
    setSupportActionBar(toolbar)

    towers = Towers(
      fm = supportFragmentManager,
      containerViewIds = listOf(R.id.v_master, R.id.v_detail),
      listener = this,
      inState = savedInstanceState, // If you call towers.save() with onSaveInstanceState, it will automatically restore here!
      notify = true
    )

    // If we are starting fresh, there wont be any fragments yet, so initialize the state
    if (towers.emptyAt(masterTower)) {
      towers.push(masterTower, ForestFragment.newInstance(), "Forest")
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    towers.save(outState) // By calling this we can restore our state during onCreate()
    super.onSaveInstanceState(outState)
  }

  override fun towerEmpty(towers: Towers, tower: Int) {
    if (tower == masterTower) {
      onBackPressed() //Nothing left in our master stack, go back
    }
  }

  override fun towerNoLongerEmpty(towers: Towers, tower: Int) {
  }

  override fun towerNewFragment(towers: Towers, tower: Int, fragment: Fragment, title: String?) {
    // Every time a new tower is put on a stack, we can use an optional title to update our toolbar title
    title?.let { setTitle(it) }
  }

  override fun newTreePicked(tree: Tree) {
    towers.push(detailTower, TreeFragment.newInstance(tree.id), tree.name)
  }

  companion object {
    const val masterTower = 0
    const val detailTower = 1
  }
}
