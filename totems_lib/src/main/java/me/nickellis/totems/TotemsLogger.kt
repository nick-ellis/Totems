package me.nickellis.totems


interface TotemsLogger {
  fun onError(th: Throwable?, errorMessage: String)
}