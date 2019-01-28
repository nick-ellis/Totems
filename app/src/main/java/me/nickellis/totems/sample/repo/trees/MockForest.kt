package me.nickellis.totems.sample.repo.trees

import me.nickellis.totems.sample.data.Tree
import me.nickellis.totems.sample.ktx.isValidIndexOf


class MockForest private constructor(): ForestRepo {

  private val trees = mutableListOf(
    Tree(name = "Acacia"),
    Tree(name = "Alder"),
    Tree(name = "Ash"),
    Tree(name = "Aspen"),
    Tree(name = "Basswood"),
    Tree(name = "Birch"),
    Tree(name = "Buckeye"),
    Tree(name = "Catalpa"),
    Tree(name = "Cedar"),
    Tree(name = "Cottonwood"),
    Tree(name = "Eucalypts"),
    Tree(name = "Fir"),
    Tree(name = "Hemlock")
  )

  override fun findTrees(): List<Tree> = trees.toList()

  override fun findTree(id: String): Tree? = trees.firstOrNull { it.id == id }

  override fun cutDownTree(id: String): Tree? {
    val index = trees.indexOfFirst { it.id == id }
    if (index.isValidIndexOf(trees)) {
      return trees.removeAt(index)
    }
    return null
  }

  override fun plantTree(seed: Tree): Tree {
    if (trees.firstOrNull { it.id == seed.id } != null) {
      return seed
    }
    trees.add(seed)
    return trees.last()
  }

  companion object {
    private var INSTANCE: MockForest? = null
    fun getInstance(): MockForest {
      val forest = INSTANCE ?: MockForest()
      if (INSTANCE == null) {
        INSTANCE = forest
      }
      return forest
    }
  }
}