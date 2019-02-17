# Tower
Easily add, remove, and replace fragments to a fragment stack (Tower). A simple fragment manager that handles the work 
of fragment transactions. Towers essentially wraps Android's fragment manager in a way to make it more user friendly.

Pros:
- [x] No need to bother with the `FragmentManager`
- [x] Write less code
- [x] Restore fragment state easily 
- [x] Simple navigation stack

Cons:
- [ ] `AppCompatActivity` support only
- [ ] Navigation stack needs more features


## Download
```groovy
dependencies {
  implementation 'me.nickellis:towers:<VERSION>'
}
```

## Demo

## Usage
1. Implement `Towers.Listener`
```kotlin
override fun towerEmpty(towers: Towers, tower: Int) {}
override fun towerNoLongerEmpty(towers: Towers, tower: Int) {}
override fun towerNewFragment(towers: Towers, tower: Int, fragment: Fragment, title: String?) {}
```

2. Initialize towers when `onCreate` is called in your `AppCompatActivity` (currently there is no support for a 
non-support version of the fragment manager).
```kotlin
towers = Towers(
   fm = supportFragmentManager,
   containerViewIds = listOf(R.id.v_master, R.id.v_detail), //For every container ID given, a nav stack is created
   listener = this, // Have your activity implement Towers.Listener, or an object of your choosing.
   inState = savedInstanceState, // If you call towers.save() with onSaveInstanceState, it will automatically restore here!
   notify = true // Would you like the listeners called on initialization?
)
```
3. Use!
```kotlin
// If there's no fragment in the first stack, add one    
if (towers.emptyAt(0)) { 
   towers.push(0, ForestFragment.newInstance(), "Forest")
}
// Actually, nevermind
towers.pop(0)
```


License
-------

    Copyright 2016 Nick Ellis

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
