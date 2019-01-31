# Totems
The simple fragment manager that makes transactions easy. Easily manage and restore multiple fragment stacks in your activity. Totems essentially wraps Android's fragment manager in a way to make it more user friendly.

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
  implementation 'me.nickellis:totems:<VERSION>'
}
```

## Demo

## Usage
1. Implement `Totems.Listener`
```kotlin
override fun totemEmpty(totems: Totems, totem: Int) {}
override fun totemNoLongerEmpty(totems: Totems, totem: Int) {}
override fun totemNewFragment(totems: Totems, totem: Int, fragment: Fragment, title: String?) {}
```

2. Initialize totems when `onCreate` is called in your `AppCompatActivity` (currently there is no support for a non-support version of the fragment manager).
```kotlin
totems = Totems(
   fm = supportFragmentManager,
   containerViewIds = listOf(R.id.v_master, R.id.v_detail), //For every container ID given, a nav stack is created
   listener = this, // Have your activity implement Totems.Listener, or an object of your choosing.
   inState = savedInstanceState, // If you call totems.save() with onSaveInstanceState, it will automatically restore here!
   notify = true // Would you like the listeners called on inialization?
)
```
3. Use!
```kotlin
// If there's no fragment in the first stack, add one    
if (totems.totemIsEmpty(0)) { 
   totems.push(0, ForestFragment.newInstance(), "Forest")
}
// Actually, nevermind
totems.pop(0)
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
