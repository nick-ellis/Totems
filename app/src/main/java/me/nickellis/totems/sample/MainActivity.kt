package me.nickellis.totems.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    v_master_detail_demo_btn.setOnClickListener {
      Intent(this, MasterDetailActivity::class.java).also { intent ->
        startActivity(intent)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_about -> {
        Intent(this, AboutActivity::class.java)
          .run { startActivity(this) }
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }
}
