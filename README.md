# RadialGamePad

RadialGamePad is an Android library for creating gamepads overlays. It has been designed with customization in mind and it's currently powering all the layouts you see in [Lemuroid](https://github.com/Swordfish90/Lemuroid).

|Screen 1|Screen 2|Screen 3|
|---|---|---|
|![Screen1](https://github.com/Swordfish90/RadialGamePad/blob/master/screenshots/screen0.png)|![Screen2](https://github.com/Swordfish90/RadialGamePad/blob/master/screenshots/screen1.png)|![Screen3](https://github.com/Swordfish90/RadialGamePad/blob/master/screenshots/screen2.png)|

## Getting started

RadialGamePad is distributed through jitpack, modify your ```build.gradle``` file to include this:

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.swordfish90:radialgamepad:$LAST_RELEASE'
    
    // To handle the Flow of events from RadialGamePad
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
}
```

## Usage

As the name suggests, RadialGamePad is built around the idea circular dials. There is a primary dial in the center which contains a primary control such as ```Cross```, ```Stick``` or ```PrimaryButtons```, surrounded by optional secondary dials.

To define the layout a ```RadialGamePadConfig``` object is passed to the constructor, and the library will take care of sizing and positioning controls to optimize the available space.

Events are returned as a Kotlin ```Flow``` and are composed of three different types:

* **Direction**: fired from Sticks and Crosses and indicates a direction through the two components xAxis and yAxis
* **Button**: indicates when a control has been pressed or released (```KeyEvent.ACTION_DOWN``` or ```KeyEvent.ACTION_UP```)
* **Gesture**: a higher level event such as tap, double tap or triple tap

Here's a simple example which creates a remote control pad and handles its events:

```
class MainActivity : Activity() {
    private lateinit var pad: RadialGamePad

    private val padConfig = RadialGamePadConfig(
        sockets = 6,
        primaryDial = PrimaryDialConfig.Cross(
            CrossConfig(
                id = 0,
                useDiagonals = false
            )
        ),
        secondaryDials = listOf(
            SecondaryDialConfig.SingleButton(
                index = 1,
                scale = 1f,
                distance = 0f,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                    iconId = R.drawable.ic_play
                )
            ),
            SecondaryDialConfig.SingleButton(
                index = 2,
                scale = 1f,
                distance = 0f,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_L1,
                    iconId = R.drawable.ic_stop
                )
            ),
            SecondaryDialConfig.SingleButton(
                index = 4,
                scale = 1f,
                distance = 0f,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_MODE,
                    iconId = R.drawable.ic_volume_down
                )
            ),
            SecondaryDialConfig.SingleButton(
                index = 5,
                scale = 1f,
                distance = 0f,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_MODE,
                    iconId = R.drawable.ic_volume_up
                )
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        // Create a pad with default theme and 8dp of margins
        pad = RadialGamePad(padConfig, 8f, requireContext())

        findViewById<FrameLayout>(R.id.container).addView(pad)

        // Collect the Flow of events to a handler
        lifecycleScope.launch {
            pad.events()
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collect {
                    handleEvent(it)
                }
        }
    }

    private fun handleEvent(event: Event) {
        // Do something with the event.
        when (event) {
            is Event.Button -> {
                Log.d("Event", "Button event from control ${event.id}")
            }
            is Event.Gesture -> {
                Log.d("Event", "Gesture event from control ${event.id}")
            }
            is Event.Direction -> {
                Log.d("Event", "Direction event from control ${event.id}")
            }
        }
    }
}
```

### Advanced usage

Check the included app sample. If you want to see even more layouts check [Lemuroid](https://github.com/Swordfish90/Lemuroid).

### Random tips
* Do not put margins around RadialGamePad. If you need them use the margin variable, so that touch events will be still forwarded to the view
* If your gamepad is split into multiple views (very likely for games), consider using SecondaryDialConfig.Empty to enforce symmetry.
