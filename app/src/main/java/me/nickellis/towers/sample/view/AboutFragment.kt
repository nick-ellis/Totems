package me.nickellis.towers.sample.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.about_fragment.*

import me.nickellis.towers.sample.R
import me.nickellis.towers.sample.ktx.addLayout
import me.nickellis.towers.sample.view.model.AboutViewModel
import me.nickellis.towers.sample.view.model.Attribution

class AboutFragment : BaseFragment() {

  companion object {
    fun newInstance() = AboutFragment()
  }

  private lateinit var viewModel: AboutViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.about_fragment, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = baseActivity!!.viewModelFactory
      .create(AboutViewModel::class.java)

    populate(viewModel.attributions)
  }

  private fun populate(attributions: List<Attribution>) {
    v_container.removeAllViews()

    attributions.forEach { attribution ->
      v_container.addLayout<TextView>(R.layout.text_header).apply {
        text = attribution.header
      }
      v_container.addLayout<TextView>(R.layout.btn_primary).apply {
        text = attribution.link.display
        setOnClickListener {
          Intent(Intent.ACTION_VIEW)
            .apply { Uri.parse(attribution.link.url) }
            .run { startActivity(this) }
        }
      }
    }
  }

}
