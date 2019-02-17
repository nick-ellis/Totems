package me.nickellis.towers.sample.repo.trees

import me.nickellis.towers.sample.data.Tree


interface ForestRepo {
  fun findTrees(): List<Tree>
  fun findTree(id: String): Tree?
  fun cutDownTree(id: String): Tree?
  fun plantTree(seed: Tree): Tree
}