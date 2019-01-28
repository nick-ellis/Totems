package me.nickellis.totems.sample.repo.trees

import me.nickellis.totems.sample.data.Tree


interface ForestRepo {
  fun findTrees(): List<Tree>
  fun findTree(id: String): Tree?
  fun cutDownTree(id: String): Tree?
  fun plantTree(seed: Tree): Tree
}