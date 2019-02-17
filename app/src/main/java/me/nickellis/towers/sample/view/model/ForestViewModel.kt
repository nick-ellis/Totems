package me.nickellis.towers.sample.view.model

import androidx.lifecycle.ViewModel
import me.nickellis.towers.sample.data.Tree

class ForestViewModel : ViewModel() {
  val forest: MutableList<Tree> = mutableListOf()
}
