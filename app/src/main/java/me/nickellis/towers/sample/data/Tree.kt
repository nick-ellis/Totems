package me.nickellis.towers.sample.data

import java.util.*


data class Tree(
  val name: String
) {
  val id: String = UUID.randomUUID().toString()
}