package me.nickellis.towers


interface TowersLogger {
  fun onError(th: Throwable?, errorMessage: String)
}