package me.nickellis.totems.sample.view.model

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.Exception


class ViewModelFactory(private val r: Resources): ViewModelProvider.Factory {

  override fun <T : ViewModel?> create(modelClass: Class<T>): T {
    try {
      @Suppress("UNCHECKED_CAST")
      return providerMap[modelClass]?.invoke() as T
    } catch (ex: Exception) {
      throw IllegalAccessException("Provider map does not include a model $modelClass")
    }
  }

  private val providerMap: Map<Class<out ViewModel>, () -> ViewModel> = mapOf(
    viewModelEntry(AboutViewModel::class.java) { AboutViewModel(r) },
    viewModelEntry(TreeViewModel::class.java) { TreeViewModel() },
    viewModelEntry(ForestViewModel::class.java) { ForestViewModel() }
  )

  private fun <T : ViewModel> viewModelEntry(clazz: Class<T>, provider: () -> T) = Pair(clazz, provider)
}