package me.nickellis.towers

import android.util.Log

class DefaultLogger(private val tag: String) : TowersLogger {
  override fun onError(th: Throwable?, errorMessage: String) {
    th?.let { Log.e(tag, errorMessage, it) } ?: Log.e(tag, errorMessage)
  }
}