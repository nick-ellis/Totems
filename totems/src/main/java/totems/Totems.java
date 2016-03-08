package totems;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Totems {

    //Pattern is TOTEMS_<STACK POS.>_<POS. IN STACK>, 0 is considered bottom of stack for <POS. IN STACK>
    private static final String TAG_PREFIX = "TOTEMS"; //used in regex, escape special characters
    private static final String TAG_PATTERN = "^" + TAG_PREFIX + "_[0-9]+_[0-9]+$";

    public interface Listener {
        void totemEmpty(int totem);
        void totemNoLongerEmpty(int totem);
        void totemNewFragment(int totem, Fragment fragment);
    }

    //Useful for a Totem instance with 2 totems (common for master/detail view)
    public static final int LEFT_TOTEM = 0;
    public static final int RIGHT_TOTEM = 1;

    private final Listener listener;
    private final FragmentManager fm;
    private final int[] containerViewIds;

    private final List<Stack<Fragment>> totems;

    private Totems(Listener listener, FragmentManager fm, int... containerViewIds) {
        if (containerViewIds == null || containerViewIds.length < 1) {
            throw new IllegalArgumentException("Please supply at least 1 container view ID");
        }

        this.listener = listener;
        this.fm = fm;
        this.containerViewIds = containerViewIds;

        totems = new ArrayList<>(containerViewIds.length);
        for (int containerViewId : containerViewIds) totems.add(new Stack<Fragment>());

        resume();
    }
    private void resume() {
        if (fm.getFragments() != null) {
            for (Fragment fragment : fm.getFragments()) {
                if (fragment != null && fragment.getTag().matches(TAG_PATTERN)) {

                    String[] split = fragment.getTag().split("_");
                    int totemLoc = Integer.parseInt(split[1]);
                    int location = Integer.parseInt(split[2]);

                    if (totemLoc < totems.size()) {
                        Stack<Fragment> totem = totems.get(totemLoc);
                        while (location >= totem.size())
                            totem.push(null);

                        totem.set(location, fragment);
                    }
                }
            }
        }
    }

    /**
     * Creates N number of totems, where N is the number of containerViewIds supplied. Will resume
     * any previous state of totems with the {@code FragmentManager} supplied.
     * @param listener Object that will be listening to Totems's callbacks
     * @param fm FragmentManager (from support library)
     * @param containerViewIds The {@code R.id} of the views you wish your totems to interact with
     * @return A new instance of Totems that resumes the previous state if present
     */
    public static Totems newTotems(Listener listener, FragmentManager fm, int... containerViewIds) {
        return new Totems(listener, fm, containerViewIds);
    }

    /**
     * Returns the number of totems. This will be equal to the number of container IDs supplied
     * @return Number of totems
     */
    public int totemCount() {
        return totems.size();
    }

    /**
     * Measures the height of a totem
     * @param totemLoc Location of totem to be measured
     * @return The height (size) of a totem
     */
    public int totemHeight(int totemLoc) {
        return totems.get(totemLoc).size();
    }

    /**
     * Pushes a fragment on a totem
     * @param totemLoc Totem location to push a fragment on
     * @param fragment Fragment to be pushed on
     * @return Totems instance
     */
    public Totems push(int totemLoc, Fragment fragment) {
        return push(totemLoc, fragment, true);
    }

    /**
     * Pushes a fragment on a totem
     * @param totemLoc Totem location to push a fragment on
     * @param fragment Fragment to be pushed on
     * @param callListener True to call listener, else false
     * @return Totems instance
     */
    public Totems push(int totemLoc, Fragment fragment, boolean callListener) {
        Stack<Fragment> totem = totems.get(totemLoc);
        totem.push(fragment);
        fmAddFragment(totemLoc, totem.size() - 1, fragment);

        if (callListener) {
            if (totem.size() == 1) {
                listener.totemNoLongerEmpty(totemLoc);
            }
            listener.totemNewFragment(totemLoc, fragment);
        }

        return this;
    }

    /**
     * Pops a fragment off a totem
     * @param totemLoc Totem location to pop a fragment off
     * @return Fragment that was popped off
     */
    public Fragment pop(int totemLoc) {
        return pop(totemLoc, true);
    }

    /**
     * Pops a fragment off a totem
     * @param totemLoc Totem location to pop a fragment off
     * @param callListener True to call listener, else false
     * @return Fragment that was popped off
     */
    public Fragment pop(int totemLoc, boolean callListener) {
        if (totems.get(totemLoc).size() == 0) return null;

        Fragment fragment = totems.get(totemLoc).pop();
        fmRemoveFragment(fragment);

        if (callListener) {
            if (totems.get(totemLoc).size() == 0) {
                listener.totemEmpty(totemLoc);
            } else {
                listener.totemNewFragment(totemLoc, totems.get(totemLoc).peek());
            }
        }

        return fragment;
    }

    /**
     * Peeks at a fragment on a totem
     * @param totemLoc Totem location to peek on
     * @return Fragment on the top of the totem
     */
    public Fragment peek(int totemLoc) {
        return totems.get(totemLoc).peek();
    }

    /**
     * Finds a specific fragment within a totem
     * @param totemLoc Totem location to find the fragment from
     * @param type Class to find, (e.g. CustomFragment.class)
     * @param <T> Object that the fragment is, must extend {@code Fragment}.
     * @return Returns the first fragment found in the totem that matches the supplied {@code type}.
     *  If there is no match {@code null} is returned.
     */
    public <T extends Fragment> T find(int totemLoc, Class<T> type) {
        Stack<Fragment> totem = totems.get(totemLoc);
        for (Fragment fragment : totem) {
            if (type.isInstance(fragment))
                return (T)fragment;
        }
        return null;
    }

    /**
     * Checks a totem at {@code totemLoc} for the supplied fragment class ({@code type}).
     * @param totemLoc Totem location to find the class from
     * @param type Fragment class to search for
     * @param <T> Object that the fragment is, must extend {@code Fragment}.
     * @return True if class is found within totem, else false
     */
    public <T extends Fragment> boolean isClassInTotem(int totemLoc, Class<T> type) {
        return find(totemLoc, type) != null;
    }

    /**
     * Checks a totem at {@code totemLoc} for the supplied {@code fragment}.
     * @param totemLoc Totem location to find the fragment from
     * @param fragment Fragment to check for
     * @return True if fragment is found within totem, else false
     */
    public boolean isFragmentInTotem(int totemLoc, Fragment fragment) {
        for (Fragment totemFrag : totems.get(totemLoc))
            if (fragment == totemFrag) return true;
        return false;
    }

    /**
     * Clears all the totems from a totem
     * @param totemLoc Location of totem to be cleared
     * @return Totems instance
     */
    public Totems clear(int totemLoc) {
        return clear(totemLoc, true);
    }

    /**
     * Clears all the totems from a totem
     * @param totemLoc Location of totem to be cleared
     * @param callListener True to call listener, else false
     * @return Totems instance
     */
    public Totems clear(int totemLoc, boolean callListener) {
        Stack<Fragment> totem = totems.get(totemLoc);
        if (totem.size() > 0) {
            totem.clear();
            fmClearFragments(totemLoc);
            if (callListener) {
                listener.totemEmpty(totemLoc);
            }
        }
        return this;
    }

    /**
     * Clears the fragments of a given {@code type} from the totem
     * @param totemLoc Location of the totem to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the totem
     * @return The number of totems removed from the totem
     */
    public int clearFragmentClasses(int totemLoc, Class<?>... fragmentClasses) {
        return clearFragmentClasses(totemLoc, true, fragmentClasses);
    }

    /**
     * Clears the fragments of a given {@code type} from the totem
     * @param totemLoc Location of the totem to be cleared of particular fragment classes
     * @param fragmentClasses Fragment classes that should be removed from the totem
     * @param callListener True to call listener, else false
     * @return The number of totems removed from the totem
     */
    public int clearFragmentClasses(int totemLoc,  boolean callListener,
                                    Class<?>... fragmentClasses) {
        int numOfFragsRemoved = 0;

        Stack<Fragment> totem = totems.get(totemLoc);
        if (!totem.isEmpty()) {
            List<Class<?>> fragClasses = Arrays.asList(fragmentClasses);
            Stack<Fragment> newTotem = new Stack<>();
            Stack<Fragment> fragsToRemove = new Stack<>();

            for (Fragment fragment : totem) {
                if (fragClasses.contains(fragment.getClass())) {
                    fragsToRemove.push(fragment);
                } else {
                    newTotem.push(fragment);
                }
            }

            numOfFragsRemoved = fragsToRemove.size();
            if (numOfFragsRemoved > 0) {
                FragmentTransaction trans = fm.beginTransaction();

                while (!fragsToRemove.isEmpty())
                    trans.remove(fragsToRemove.pop());

                /*
                We can assume that if the top of the totem is detached that there are no fragments
                in the totem currently attached. Else the fragment that was attached would be above
                the fragment currently seen as detached.
                 */
                if (newTotem.size() > 0 && newTotem.peek().isDetached())
                    trans.attach(newTotem.peek());

                trans.commit();

                totems.set(totemLoc, newTotem);
                if (callListener && totem.size() > 0 && newTotem.size() == 0) {
                    listener.totemEmpty(totemLoc);
                }
                if (callListener && newTotem.size() > 0 && totem.peek() != newTotem.peek()) {
                    listener.totemNewFragment(totemLoc, newTotem.peek());
                }
            }
        }
        return numOfFragsRemoved;
    }

    /**
     * Attempts to execute any pending actions in the fragment manager. If
     * {@code executePendingTransactions()} is already in progress, it will catch the exception
     * and do nothing.
     */
    public void executePendingTotemMoves() {
        try {
            fm.executePendingTransactions();
        } catch (IllegalStateException ex) {
            //executePendingTransactions is already being ran
        }
    }

    private String getTag(int totemLoc, int location) {
        return TAG_PREFIX + "_" + totemLoc + "_" + location;
    }

    private void fmAddFragment(int totemLoc, int location, Fragment fragment) {

        FragmentTransaction trans = fm.beginTransaction();
        trans.add(containerViewIds[totemLoc], fragment, getTag(totemLoc, location));

        if (location > 0) {
            Fragment prevFragment = fm.findFragmentByTag(getTag(totemLoc, location - 1));
            trans.detach(prevFragment);
        }

        trans.commit();
    }

    private void fmRemoveFragment(Fragment fragment) {
        int totemLoc = Integer.parseInt(fragment.getTag().split("_")[1]);
        int location = Integer.parseInt(fragment.getTag().split("_")[2]);

        FragmentTransaction trans = fm.beginTransaction();
        trans.remove(fragment);

        if (location > 0) {
            Fragment prevFragment = fm.findFragmentByTag(getTag(totemLoc, location - 1));
            trans.attach(prevFragment);
        }

        trans.commit();
    }

    private void fmClearFragments(int totemLoc) {

        Stack<Fragment> fragsToRemove = new Stack<>();
        for (Fragment fragment : fm.getFragments()) {
            if (fragment != null && fragment.getTag().matches(TAG_PATTERN)) {
                int fmTotemLoc = Integer.parseInt(fragment.getTag().split("_")[1]);

                if (fmTotemLoc == totemLoc) {
                    fragsToRemove.push(fragment);
                }
            }
        }

        if (fragsToRemove.size() > 0) {
            FragmentTransaction trans = fm.beginTransaction();

            while(!fragsToRemove.empty())
                trans.remove(fragsToRemove.pop());

            trans.commit();
        }
    }
}
