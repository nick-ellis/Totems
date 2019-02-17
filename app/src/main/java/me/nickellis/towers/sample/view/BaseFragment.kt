package me.nickellis.towers.sample.view

import androidx.fragment.app.Fragment
import me.nickellis.towers.sample.BaseActivity


abstract class BaseFragment : Fragment() {

  protected val baseActivity: BaseActivity? get() = (activity as? BaseActivity)

}