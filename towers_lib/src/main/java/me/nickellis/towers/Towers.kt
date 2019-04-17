package me.nickellis.towers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creates N number of towers, where N is the number of containerViewIds supplied. Will resume
 * any previous state of towers with the `FragmentManager` supplied.
 * @param fm FragmentManager (from support library).
 * @param containerViewIds The `R.id` of the views you wish your towers to interact with.
 * @param listener Object that will be listening to Towers's callbacks.
 * @param logger For logging any [FragmentTransaction.commit] errors, by default will log to [Log].
 * @param inState To resume the fragment state, this needs to be supplied. Be sure to call [save] on your bundle.
 * @param notify Whether you want the [listener] to be called on initialization.
 * @return A new instance of Towers that resumes the previous state if present
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Towers @JvmOverloads constructor(
    private val fm: FragmentManager,
    private val containerViewIds: List<Int>,
    private val listener: Listener? = null,
    private val logger: TowersLogger = DefaultLogger("Towers"),
    inState: Bundle? = null,
    notify: Boolean = false
) {

    //Pattern is TowerS_<STACK POS.>_<POS. IN STACK>, 0 is considered bottom of stack for <POS. IN STACK>
    private val savePrefix = "TowerS" //used in regex, escape special characters
    private val tagRegex = "^" + savePrefix + "_[0-9]+_[0-9]+$"
    private val tagSave = savePrefix + "_TAGS"
    private val titleSave = savePrefix + "_TITLES"

    private val towers: MutableList<Stack<Pair<Fragment, String?>>>

    interface Listener {
        fun towerEmpty(towers: Towers, tower: Int)
        fun towerNoLongerEmpty(towers: Towers, tower: Int)
        fun towerNewFragment(towers: Towers, tower: Int, fragment: Fragment, title: String?)
    }

    init {
        if (containerViewIds.isEmpty()) {
            throw IllegalArgumentException("Please supply at least 1 container view ID")
        }

        towers = ArrayList(containerViewIds.size)
        repeat(containerViewIds.count()) { towers.add(Stack()) }

        resume(inState, notify)
    }

    private fun resume(inState: Bundle?, notify: Boolean) {
        val tags = inState?.getStringArray(tagSave) ?: return
        val titles = inState.getStringArray(titleSave) ?: emptyArray()

        tags.forEachIndexed { index, tag ->
            val (towerLoc, location) = getTagLocations(tag)

            if (towerLoc < towers.size) {
                val tower = towers[towerLoc]
                while (location >= tower.size)
                    tower.push(Pair(Fragment(), null))

                fm.findFragmentByTag(tag)?.let {
                    tower[location] = Pair(it, titles[index])
                }
            }
        }

        if (notify) {
            towers.forEachIndexed { towerLoc, stack ->
                if (stack.isNotEmpty()) {
                    val (fragment, title) = stack.peek()
                    listener?.towerNewFragment(this, towerLoc, fragment, title)
                }
            }
        }
    }

    /**
     * To save the current state of towers, call this
     * @param outState The bundle to save the state to, supply this bundle with the constructor to resume
     */
    fun save(outState: Bundle): Towers {
        val tags: ArrayList<String> = arrayListOf()
        val titles: ArrayList<String> = arrayListOf()

        towers.flatten()
            .forEach {
                tags.add(it.fragment().tag!!)
                titles.add(it.title() ?: "")
            }

        outState.putStringArray(tagSave, tags.toTypedArray())
        outState.putStringArray(titleSave, titles.toTypedArray())

        return this
    }

    /**
     * Returns the number of towers. This will be equal to the number of container IDs supplied
     * @return Number of towers
     */
    fun towerCount(): Int = towers.size

    /**
     * Measures the height of a tower
     * @param towerLoc Location of tower to be measured
     * @return The height (size) of a tower
     */
    fun heightOf(towerLoc: Int): Int = towers[towerLoc].size

    /**
     * Determines whether there are fragments in a tower
     * @param towerLoc Location of tower to be checked
     * @return If the tower is empty or not
     */
    fun emptyAt(towerLoc: Int): Boolean = heightOf(towerLoc) == 0

    /**
     * Pushes a fragment on a tower
     * @param towerLoc Tower location to push a fragment on
     * @param fragment Fragment to be pushed on
     * @param notify True to call listener, else false
     * @return Towers instance
     */
    @JvmOverloads
    fun push(towerLoc: Int, fragment: Fragment, title: String? = null, notify: Boolean = true): Towers {
        val tower = towers[towerLoc]
        tower.push(Pair(fragment, title))
        fmAddFragment(towerLoc, tower.size - 1, fragment)

        if (notify) {
            if (tower.size == 1) {
                listener?.towerNoLongerEmpty(this, towerLoc)
            }
            listener?.towerNewFragment(this, towerLoc, fragment, title)
        }

        return this
    }

    /**
     * Pops a fragment off a tower
     * @param towerLoc Tower location to pop a fragment off
     * @param notify True to call listener, else false
     * @return Fragment that was popped off
     */
    @JvmOverloads
    fun pop(towerLoc: Int, notify: Boolean = true): Fragment? {
        if (towers[towerLoc].size == 0) return null

        val (fragment, _) = towers[towerLoc].pop()
        fmRemoveFragment(fragment)

        if (notify) {
            if (towers[towerLoc].isEmpty()) {
                listener?.towerEmpty(this, towerLoc)
            } else {
                val (newFragment, title) = towers[towerLoc].peek()
                listener?.towerNewFragment(this, towerLoc, newFragment, title)
            }
        }

        return fragment
    }

    /**
     * Replaces the top fragment on the tower
     * @param towerLoc Tower location to replace fragment
     * @param notify True to call listener, else false
     * @return Fragment that was replaced
     */
    @JvmOverloads
    fun replace(towerLoc: Int, fragment: Fragment, title: String? = null, notify: Boolean = true): Fragment {
        val tower = towers[towerLoc]
        val (replaced, _) = tower.pop()
        tower.push(Pair(fragment, title))
        fmReplaceFragment(towerLoc, tower.size - 1, fragment, replaced)

        if (notify) {
            if (tower.size == 1) {
                listener?.towerNoLongerEmpty(this, towerLoc)
            }
            listener?.towerNewFragment(this, towerLoc, fragment, title)
        }

        return replaced
    }

    /**
     * Peeks at a fragment on a tower
     * @param towerLoc Tower location to peek on
     * @return Fragment on the top of the tower, or null if there is no fragment
     */
    fun peek(towerLoc: Int): Fragment? =
        if (towers[towerLoc].isNotEmpty()) towers[towerLoc].peek().fragment() else null


    /**
     * Finds a specific fragment within a tower
     * @param towerLoc Tower location to find the fragment from
     * @param type Class to find, (e.g. CustomFragment.class)
     * @param <T> Object that the fragment is, must extend `Fragment`.
     * @return Returns the first fragment found in the tower that matches the supplied `type`.
     * If there is no match `null` is returned.
     **/
    @Suppress("UNCHECKED_CAST")
    fun <T : Fragment> find(towerLoc: Int, type: Class<T>): T? {
        val fragment = towers[towerLoc].firstOrNull { type.isInstance(it.fragment()) }?.fragment()
        return if (fragment == null) null else fragment as T
    }

    /**
     * Checks a tower at `towerLoc` for the supplied fragment class (`type`).
     * @param towerLoc Tower location to find the class from
     * @param type Fragment class to search for
     * @param <T> Object that the fragment is, must extend `Fragment`.
     * @return True if class is found within tower, else false
     **/
    fun <T : Fragment> isClassInTower(towerLoc: Int, type: Class<T>): Boolean =
        find(towerLoc, type) != null

    /**
     * Checks a tower at `towerLoc` for the supplied `fragment`.
     * @param towerLoc Tower location to find the fragment from
     * @param fragment Fragment to check for
     * @return True if fragment is found within tower, else false
     */
    fun isFragmentInTower(towerLoc: Int, fragment: Fragment): Boolean =
        towers[towerLoc].any { fragment === it.fragment() }

    /**
     * Clears all the towers from a tower
     * @param towerLoc Location of tower to be cleared
     * @param notify True to call listener, else false
     * @return Towers instance
     */
    @JvmOverloads
    fun clear(towerLoc: Int, notify: Boolean = true): Towers {
        val tower = towers[towerLoc]
        if (tower.isNotEmpty()) {
            fmClearFragments(towerLoc)
            tower.clear()
            if (notify) {
                listener?.towerEmpty(this, towerLoc)
            }
        }
        return this
    }

    /**
     * Clears the fragments of a given `type` from the tower
     * @param towerLoc Location of the tower to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the tower
     * @return The number of towers removed from the tower
     */
    fun clearFragmentClasses(towerLoc: Int, vararg fragmentClasses: Class<*>): Int =
        clearFragmentClasses(towerLoc, true, *fragmentClasses)

    /**
     * Clears the fragments of a given `type` from the tower
     * @param towerLoc Location of the tower to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the tower
     * @param notify True to call listener, else false
     * @return The number of towers removed from the tower
     */
    fun clearFragmentClasses(towerLoc: Int, notify: Boolean, vararg fragmentClasses: Class<*>): Int {
        var numOfFragsRemoved = 0

        val tower = towers[towerLoc]
        if (!tower.isEmpty()) {
            val fragClasses = Arrays.asList(*fragmentClasses)
            val newTower = Stack<Pair<Fragment, String?>>()
            val fragsToRemove = Stack<Pair<Fragment, String?>>()

            for (pair in tower) {
                if (fragClasses.contains(pair.fragment().javaClass)) {
                    fragsToRemove.push(pair)
                } else {
                    newTower.push(pair)
                }
            }

            numOfFragsRemoved = fragsToRemove.size
            if (numOfFragsRemoved > 0) {
                val trans = fm.beginTransaction()

                while (!fragsToRemove.isEmpty())
                    trans.remove(fragsToRemove.pop().fragment())

                /*
                We can assume that if the top of the tower is detached that there are no fragments
                in the tower currently attached. Else the fragment that was attached would be above
                the fragment currently seen as detached.
                 */
                if (newTower.size > 0 && newTower.peek().fragment().isDetached)
                    trans.attach(newTower.peek().fragment())

                trans.tryCommit()

                towers[towerLoc] = newTower
                if (notify && tower.size > 0 && newTower.size == 0) {
                    listener?.towerEmpty(this, towerLoc)
                }
                if (notify && newTower.size > 0 && tower.peek() !== newTower.peek()) {
                    listener?.towerNewFragment(this, towerLoc,
                        newTower.peek().fragment(), newTower.peek().title())
                }
            }
        }
        return numOfFragsRemoved
    }

    /**
     * Attempts to execute any pending actions in the fragment manager. If
     * `executePendingTransactions()` is already in progress, it will catch the exception
     * and do nothing.
     */
    fun executePendingTowerMoves() {
        try {
            fm.executePendingTransactions()
        } catch (ex: IllegalStateException) {
            //executePendingTransactions is already being ran
        }
    }

    private fun Pair<Fragment, String?>.fragment(): Fragment = this.first
    private fun Pair<Fragment, String?>.title(): String? = this.second

    private fun getFragmentTag(towerLoc: Int, location: Int): String =
        savePrefix + getTagSuffix(towerLoc, location)

    private fun getTagSuffix(towerLoc: Int, location: Int): String =
        "_" + towerLoc + "_" + location

    private fun getTagLocations(fragmentTag: String): Pair<Int, Int> {
        val split = fragmentTag
            .split("_".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        return Pair(split[split.lastIndex - 1].toInt(), split.last().toInt())
    }

    private fun fmReplaceFragment(towerLoc: Int, location: Int, newFragment: Fragment, oldFragment: Fragment) {
        val trans = fm.beginTransaction()
        trans.remove(oldFragment)
        trans.add(containerViewIds[towerLoc], newFragment, getFragmentTag(towerLoc, location))
        trans.tryCommit()
    }

    private fun fmAddFragment(towerLoc: Int, location: Int, fragment: Fragment) {

        val trans = fm.beginTransaction()
        trans.add(containerViewIds[towerLoc], fragment, getFragmentTag(towerLoc, location))

        if (location > 0) {
            fm.findFragmentByTag(getFragmentTag(towerLoc, location - 1))?.let { previous ->
                trans.detach(previous)
            }
        }

        trans.tryCommit()
    }

    private fun fmRemoveFragment(fragment: Fragment) {
        val (towerLoc, location) = getTagLocations(fragment.tag!!)

        val trans = fm.beginTransaction()
        trans.remove(fragment)

        if (location > 0) {
            fm.findFragmentByTag(getFragmentTag(towerLoc, location - 1))?.let { previous ->
                trans.attach(previous)
            }
        }

        trans.tryCommit()
    }

    private fun fmClearFragments(towerLoc: Int) {
        val fragsToRemove = towers[towerLoc].map { it.fragment() }

        if (fragsToRemove.isNotEmpty()) {
            val trans = fm.beginTransaction()
            fragsToRemove.forEach { trans.remove(it) }
            trans.tryCommit()
        }
    }

    private fun FragmentTransaction.tryCommit() {
        try {
            commit()
        } catch (ise: IllegalStateException) {
            try {
                commitAllowingStateLoss()
                logger.onError(ise, "Unable to commit transaction without state loss")
            } catch (ex: Exception) {
                logger.onError(ex, "Transaction commit error")
            }
        } catch (ex: Exception) {
            logger.onError(ex, "Transaction commit error")
        }
    }
}
