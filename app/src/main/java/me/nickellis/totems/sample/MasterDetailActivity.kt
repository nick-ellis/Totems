package me.nickellis.totems.sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import me.nickellis.totems.Totems
import me.nickellis.totems.sample.data.Tree
import me.nickellis.totems.sample.view.ForestFragment
import me.nickellis.totems.sample.view.TreeFragment

class MasterDetailActivity : BaseActivity(), Totems.Listener, ForestFragment.Listener {

  private lateinit var totems: Totems

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_master_detail)
    setSupportActionBar(toolbar)

    totems = Totems(
      fm = supportFragmentManager,
      containerViewIds = listOf(R.id.v_master, R.id.v_detail),
      listener = this,
      inState = savedInstanceState, // If you call totems.save() with onSaveInstanceState, it will automatically restore here!
      notify = true
    )

    // If we are starting fresh, there wont be any fragments yet, so initialize the state
    if (totems.totemIsEmpty(masterTotem)) {
      totems.push(masterTotem, ForestFragment.newInstance(), "Forest")
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    totems.save(outState) // By calling this we can restore our state during onCreate()
    super.onSaveInstanceState(outState)
  }

  override fun totemEmpty(totems: Totems, totem: Int) {
    if (totem == masterTotem) {
      onBackPressed() //Nothing left in our master stack, go back
    }
  }

  override fun totemNoLongerEmpty(totems: Totems, totem: Int) {
  }

  override fun totemNewFragment(totems: Totems, totem: Int, fragment: Fragment, title: String?) {
    // Every time a new totem is put on a stack, we can use an optional title to update our toolbar title
    title?.let { setTitle(it) }
  }

  override fun newTreePicked(tree: Tree) {
    totems.push(detailTotem, TreeFragment.newInstance(tree.id), tree.name)
  }

  companion object {
    const val masterTotem = 0
    const val detailTotem = 1
  }
}
