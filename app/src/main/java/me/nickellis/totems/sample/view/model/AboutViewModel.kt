package me.nickellis.totems.sample.view.model

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import me.nickellis.totems.sample.R

class AboutViewModel(private val r: Resources) : ViewModel() {

  val attributions = listOf(
    Attribution(
      header = r.getString(R.string.app_uses_fontawesome),
      link = Link(
        display = r.getString(R.string.license),
        url = "https://fontawesome.com/license"
      )
    )
  )

}

class Attribution(
  val header: String,
  val link: Link
)

class Link(
  val display: String,
  val url: String
)