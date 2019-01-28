package me.nickellis.totems.sample.view.model

import androidx.lifecycle.ViewModel
import me.nickellis.totems.sample.data.Tree

class ForestViewModel : ViewModel() {
  val forest: MutableList<Tree> = mutableListOf()
}
