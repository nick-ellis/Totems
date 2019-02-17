package me.nickellis.towers.sample.ktx


fun <T> Int.isValidIndexOf(collection: Collection<T>) = this in 0 until collection.size