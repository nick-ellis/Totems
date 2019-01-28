package me.nickellis.totems

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.util.*
import kotlin.collections.ArrayList

/**
 * Creates N number of totems, where N is the number of containerViewIds supplied. Will resume
 * any previous state of totems with the `FragmentManager` supplied.
 * @param fm FragmentManager (from support library).
 * @param containerViewIds The `R.id` of the views you wish your totems to interact with.
 * @param listener Object that will be listening to Totems's callbacks.
 * @param logger For logging any [FragmentTransaction.commit] errors, by default will log to [Log].
 * @param inState To resume the fragment state, this needs to be supplied. Be sure to call [save] on your bundle.
 * @param notify Whether you want the [listener] to be called on initialization.
 * @return A new instance of Totems that resumes the previous state if present
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class Totems @JvmOverloads constructor(
    private val fm: FragmentManager,
    private val containerViewIds: List<Int>,
    private val listener: Listener? = null,
    private val logger: TotemsLogger = DefaultLogger("Totems"),
    inState: Bundle? = null,
    notify: Boolean = false
) {

    //Pattern is TOTEMS_<STACK POS.>_<POS. IN STACK>, 0 is considered bottom of stack for <POS. IN STACK>
    private val savePrefix = "TOTEMS" //used in regex, escape special characters
    private val tagRegex = "^" + savePrefix + "_[0-9]+_[0-9]+$"
    private val tagSave = savePrefix + "_TAGS"
    private val titleSave = savePrefix + "_TITLES"

    private val totems: MutableList<Stack<Pair<Fragment, String?>>>

    interface Listener {
        fun totemEmpty(totems: Totems, totem: Int)
        fun totemNoLongerEmpty(totems: Totems, totem: Int)
        fun totemNewFragment(totems: Totems, totem: Int, fragment: Fragment, title: String?)
    }

    init {
        if (containerViewIds.isEmpty()) {
            throw IllegalArgumentException("Please supply at least 1 container view ID")
        }

        totems = ArrayList(containerViewIds.size)
        repeat(containerViewIds.count()) { totems.add(Stack()) }

        resume(inState, notify)
    }

    private fun resume(inState: Bundle?, notify: Boolean) {
        val tags = inState?.getStringArray(tagSave) ?: return
        val titles = inState.getStringArray(titleSave) ?: emptyArray()

        tags.forEachIndexed { index, tag ->
            val (totemLoc, location) = getTagLocations(tag)

            if (totemLoc < totems.size) {
                val totem = totems[totemLoc]
                while (location >= totem.size)
                    totem.push(Pair(Fragment(), null))

                fm.findFragmentByTag(tag)?.let {
                    totem[location] = Pair(it, titles[index])
                }
            }
        }

        if (notify) {
            totems.forEachIndexed { totemLoc, stack ->
                if (stack.isNotEmpty()) {
                    val (fragment, title) = stack.peek()
                    listener?.totemNewFragment(this, totemLoc, fragment, title)
                }
            }
        }
    }

    /**
     * To save the current state of totems, call this
     * @param outState The bundle to save the state to, supply this bundle with the constructor to resume
     */
    fun save(outState: Bundle): Totems {
        val tags: ArrayList<String> = arrayListOf()
        val titles: ArrayList<String> = arrayListOf()

        totems.flatten()
            .forEach {
                tags.add(it.fragment().tag!!)
                titles.add(it.title() ?: "")
            }

        outState.putStringArray(tagSave, tags.toTypedArray())
        outState.putStringArray(titleSave, titles.toTypedArray())

        return this
    }

    /**
     * Returns the number of totems. This will be equal to the number of container IDs supplied
     * @return Number of totems
     */
    fun numberOfTotems(): Int = totems.size

    /**
     * Measures the height of a totem
     * @param totemLoc Location of totem to be measured
     * @return The height (size) of a totem
     */
    fun totemHeight(totemLoc: Int): Int = totems[totemLoc].size

    /**
     * Determines whether there are fragments in a totem
     * @param totemLoc Location of totem to be checked
     * @return If the totem is empty or not
     */
    fun totemIsEmpty(totemLoc: Int): Boolean = totemHeight(totemLoc) == 0

    /**
     * Pushes a fragment on a totem
     * @param totemLoc Totem location to push a fragment on
     * @param fragment Fragment to be pushed on
     * @param notify True to call listener, else false
     * @return Totems instance
     */
    @JvmOverloads
    fun push(totemLoc: Int, fragment: Fragment, title: String? = null, notify: Boolean = true): Totems {
        val totem = totems[totemLoc]
        totem.push(Pair(fragment, title))
        fmAddFragment(totemLoc, totem.size - 1, fragment)

        if (notify) {
            if (totem.size == 1) {
                listener?.totemNoLongerEmpty(this, totemLoc)
            }
            listener?.totemNewFragment(this, totemLoc, fragment, title)
        }

        return this
    }

    /**
     * Pops a fragment off a totem
     * @param totemLoc Totem location to pop a fragment off
     * @param notify True to call listener, else false
     * @return Fragment that was popped off
     */
    @JvmOverloads
    fun pop(totemLoc: Int, notify: Boolean = true): Fragment? {
        if (totems[totemLoc].size == 0) return null

        val (fragment, _) = totems[totemLoc].pop()
        fmRemoveFragment(fragment)

        if (notify) {
            if (totems[totemLoc].isEmpty()) {
                listener?.totemEmpty(this, totemLoc)
            } else {
                val (newFragment, title) = totems[totemLoc].peek()
                listener?.totemNewFragment(this, totemLoc, newFragment, title)
            }
        }

        return fragment
    }

    /**
     * Replaces the top fragment on the totem
     * @param totemLoc Totem location to replace fragment
     * @param notify True to call listener, else false
     * @return Fragment that was replaced
     */
    @JvmOverloads
    fun replace(totemLoc: Int, fragment: Fragment, title: String? = null, notify: Boolean = true): Fragment {
        val totem = totems[totemLoc]
        val (replaced, _) = totem.pop()
        totem.push(Pair(fragment, title))
        fmReplaceFragment(totemLoc, totem.size - 1, fragment, replaced)

        if (notify) {
            if (totem.size == 1) {
                listener?.totemNoLongerEmpty(this, totemLoc)
            }
            listener?.totemNewFragment(this, totemLoc, fragment, title)
        }

        return replaced
    }

    /**
     * Peeks at a fragment on a totem
     * @param totemLoc Totem location to peek on
     * @return Fragment on the top of the totem, or null if there is no fragment
     */
    fun peek(totemLoc: Int): Fragment? =
        if (totems[totemLoc].isNotEmpty()) totems[totemLoc].peek().fragment() else null


    /**
     * Finds a specific fragment within a totem
     * @param totemLoc Totem location to find the fragment from
     * @param type Class to find, (e.g. CustomFragment.class)
     * @param <T> Object that the fragment is, must extend `Fragment`.
     * @return Returns the first fragment found in the totem that matches the supplied `type`.
     * If there is no match `null` is returned.
     **/
    @Suppress("UNCHECKED_CAST")
    fun <T : Fragment> find(totemLoc: Int, type: Class<T>): T? {
        val fragment = totems[totemLoc].firstOrNull { type.isInstance(it.fragment()) }?.fragment()
        return if (fragment == null) null else fragment as T
    }

    /**
     * Checks a totem at `totemLoc` for the supplied fragment class (`type`).
     * @param totemLoc Totem location to find the class from
     * @param type Fragment class to search for
     * @param <T> Object that the fragment is, must extend `Fragment`.
     * @return True if class is found within totem, else false
     **/
    fun <T : Fragment> isClassInTotem(totemLoc: Int, type: Class<T>): Boolean =
        find(totemLoc, type) != null

    /**
     * Checks a totem at `totemLoc` for the supplied `fragment`.
     * @param totemLoc Totem location to find the fragment from
     * @param fragment Fragment to check for
     * @return True if fragment is found within totem, else false
     */
    fun isFragmentInTotem(totemLoc: Int, fragment: Fragment): Boolean =
        totems[totemLoc].any { fragment === it.fragment() }

    /**
     * Clears all the totems from a totem
     * @param totemLoc Location of totem to be cleared
     * @param notify True to call listener, else false
     * @return Totems instance
     */
    @JvmOverloads
    fun clear(totemLoc: Int, notify: Boolean = true): Totems {
        val totem = totems[totemLoc]
        if (totem.isNotEmpty()) {
            fmClearFragments(totemLoc)
            totem.clear()
            if (notify) {
                listener?.totemEmpty(this, totemLoc)
            }
        }
        return this
    }

    /**
     * Clears the fragments of a given `type` from the totem
     * @param totemLoc Location of the totem to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the totem
     * @return The number of totems removed from the totem
     */
    fun clearFragmentClasses(totemLoc: Int, vararg fragmentClasses: Class<*>): Int =
        clearFragmentClasses(totemLoc, true, *fragmentClasses)

    /**
     * Clears the fragments of a given `type` from the totem
     * @param totemLoc Location of the totem to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the totem
     * @param notify True to call listener, else false
     * @return The number of totems removed from the totem
     */
    fun clearFragmentClasses(totemLoc: Int, notify: Boolean, vararg fragmentClasses: Class<*>): Int {
        var numOfFragsRemoved = 0

        val totem = totems[totemLoc]
        if (!totem.isEmpty()) {
            val fragClasses = Arrays.asList(*fragmentClasses)
            val newTotem = Stack<Pair<Fragment, String?>>()
            val fragsToRemove = Stack<Pair<Fragment, String?>>()

            for (pair in totem) {
                if (fragClasses.contains(pair.fragment().javaClass)) {
                    fragsToRemove.push(pair)
                } else {
                    newTotem.push(pair)
                }
            }

            numOfFragsRemoved = fragsToRemove.size
            if (numOfFragsRemoved > 0) {
                val trans = fm.beginTransaction()

                while (!fragsToRemove.isEmpty())
                    trans.remove(fragsToRemove.pop().fragment())

                /*
                We can assume that if the top of the totem is detached that there are no fragments
                in the totem currently attached. Else the fragment that was attached would be above
                the fragment currently seen as detached.
                 */
                if (newTotem.size > 0 && newTotem.peek().fragment().isDetached)
                    trans.attach(newTotem.peek().fragment())

                tryCommit(trans)

                totems[totemLoc] = newTotem
                if (notify && totem.size > 0 && newTotem.size == 0) {
                    listener?.totemEmpty(this, totemLoc)
                }
                if (notify && newTotem.size > 0 && totem.peek() !== newTotem.peek()) {
                    listener?.totemNewFragment(this, totemLoc,
                        newTotem.peek().fragment(), newTotem.peek().title())
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
    fun executePendingTotemMoves() {
        try {
            fm.executePendingTransactions()
        } catch (ex: IllegalStateException) {
            //executePendingTransactions is already being ran
        }
    }

    private fun Pair<Fragment, String?>.fragment(): Fragment = this.first
    private fun Pair<Fragment, String?>.title(): String? = this.second

    private fun getFragmentTag(totemLoc: Int, location: Int): String =
        savePrefix + getTagSuffix(totemLoc, location)

    private fun getTagSuffix(totemLoc: Int, location: Int): String =
        "_" + totemLoc + "_" + location

    private fun getTagLocations(fragmentTag: String): Pair<Int, Int> {
        val split = fragmentTag
            .split("_".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

        return Pair(split[split.lastIndex - 1].toInt(), split.last().toInt())
    }

    private fun fmReplaceFragment(totemLoc: Int, location: Int, newFragment: Fragment, oldFragment: Fragment) {
        val trans = fm.beginTransaction()
        trans.remove(oldFragment)
        trans.add(containerViewIds[totemLoc], newFragment, getFragmentTag(totemLoc, location))
        tryCommit(trans)
    }

    private fun fmAddFragment(totemLoc: Int, location: Int, fragment: Fragment) {

        val trans = fm.beginTransaction()
        trans.add(containerViewIds[totemLoc], fragment, getFragmentTag(totemLoc, location))

        if (location > 0) {
            fm.findFragmentByTag(getFragmentTag(totemLoc, location - 1))?.let { previous ->
                trans.detach(previous)
            }
        }

        tryCommit(trans)
    }

    private fun fmRemoveFragment(fragment: Fragment) {
        val (totemLoc, location) = getTagLocations(fragment.tag!!)

        val trans = fm.beginTransaction()
        trans.remove(fragment)

        if (location > 0) {
            fm.findFragmentByTag(getFragmentTag(totemLoc, location - 1))?.let { previous ->
                trans.attach(previous)
            }
        }

        tryCommit(trans)
    }

    private fun fmClearFragments(totemLoc: Int) {
        val fragsToRemove = totems[totemLoc].map { it.fragment() }

        if (fragsToRemove.isNotEmpty()) {
            val trans = fm.beginTransaction()
            fragsToRemove.forEach { trans.remove(it) }
            tryCommit(trans)
        }
    }

    private fun tryCommit(transaction: FragmentTransaction) {
        try {
            transaction.commit()
        } catch (ise: IllegalStateException) {
            try {
                transaction.commitAllowingStateLoss()
                logger.onError(ise, "Unable to commit transaction without state loss")
            } catch (ex: Exception) {
                logger.onError(ex, "Transaction commit error")
            }
        } catch (ex: Exception) {
            logger.onError(ex, "Transaction commit error")
        }
    }
}
