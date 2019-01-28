package me.nickellis.totems

import android.util.Log

class DefaultLogger(private val tag: String) : TotemsLogger {
  override fun onError(th: Throwable?, errorMessage: String) {
    th?.let { Log.e(tag, errorMessage, it) } ?: Log.e(tag, errorMessage)
  }
}