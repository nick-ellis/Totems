package me.nickellis.towers.sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_about.*
import me.nickellis.towers.Towers
import me.nickellis.towers.sample.view.AboutFragment

class AboutActivity : BaseActivity(), Towers.Listener {

  private lateinit var towers: Towers

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)
    setSupportActionBar(toolbar)

    towers = Towers(
      fm = supportFragmentManager,
      containerViewIds = listOf(R.id.v_container),
      listener = this,
      inState = savedInstanceState,
      notify = true
    )

    if (towers.emptyAt(0)) {
      towers.push(0, AboutFragment.newInstance(), "About")
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    towers.save(outState)
    super.onSaveInstanceState(outState)
  }

  override fun towerEmpty(towers: Towers, tower: Int) {
    onBackPressed()
  }

  override fun towerNoLongerEmpty(towers: Towers, tower: Int) {
  }

  override fun towerNewFragment(towers: Towers, tower: Int, fragment: Fragment, title: String?) {
    title?.let { setTitle(it) }
  }
}
