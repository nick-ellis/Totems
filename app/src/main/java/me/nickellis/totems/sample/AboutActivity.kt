package me.nickellis.totems.sample

import android.os.Bundle
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_about.*
import me.nickellis.totems.Totems
import me.nickellis.totems.sample.view.AboutFragment

class AboutActivity : BaseActivity(), Totems.Listener {

  private lateinit var totems: Totems

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_about)
    setSupportActionBar(toolbar)

    totems = Totems(
      fm = supportFragmentManager,
      containerViewIds = listOf(R.id.v_container),
      listener = this,
      inState = savedInstanceState,
      notify = true
    )

    if (totems.totemIsEmpty(0)) {
      totems.push(0, AboutFragment.newInstance(), "About")
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    totems.save(outState)
    super.onSaveInstanceState(outState)
  }

  override fun totemEmpty(totems: Totems, totem: Int) {
    onBackPressed()
  }

  override fun totemNoLongerEmpty(totems: Totems, totem: Int) {
  }

  override fun totemNewFragment(totems: Totems, totem: Int, fragment: Fragment, title: String?) {
    title?.let { setTitle(it) }
  }
}
