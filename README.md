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
    implementation 'com.github.swordfish90:radialgamepad:0.1.0'
    
    // To handle Rx events from RadialGamePad
    implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
}
```

## Usage

As the name suggests, RadialGamePad is built around the idea circular dials. There is a primary dial in the center which contains a primary control such as ```Cross```, ```Stick``` or ```PrimaryButtons```, surrounded by optional secondary dials.

To define the layout a ```RadialGamePadConfig``` object is passed to the constructor, and the library will take care of sizing and positioning controls to optimize the available space.

Events are returned as an RxJava2 ```Observable``` and are composed of three different types:

* **Direction**: fired from Sticks and Crosses and indicates a direction through the two components xAxis and yAxis
* **Button**: indicates when a control has been pressed or released (```KeyEvent.ACTION_DOWN``` or ```KeyEvent.ACTION_UP```)
* **Gesture**: a higher level event such as tap, double tap or triple tap

Here's a simple example which creates a remote control pad and handles its events:

```
class MainActivity : Activity() {
    private lateinit var pad: RadialGamePad

    private val compositeDisposable = CompositeDisposable()

    private val padConfig = RadialGamePadConfig(
        // The pad will have 6 secondary dials (not every one needs to be used)
        sockets = 6,

        // Perform haptic feedback when a control is pressed
        haptic = true,

        // The primary dial is a DPad with id = 0
        primaryDial = PrimaryDialConfig.Cross(0),

        // Secondary dial are 4 buttons, with spread 1.
        secondaryDials = listOf(
            SecondaryDialConfig.SingleButton(1, 1,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_SELECT,
                    iconId = R.drawable.ic_play
                )
            ),
            SecondaryDialConfig.SingleButton(2, 1,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_L1,
                    iconId = R.drawable.ic_stop
                )
            ),
            SecondaryDialConfig.SingleButton(4, 1,
                ButtonConfig(
                    id = KeyEvent.KEYCODE_BUTTON_MODE,
                    iconId = R.drawable.ic_volume_down
                )
            ),
            SecondaryDialConfig.SingleButton(5, 1,
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
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable.add(
            pad.events().subscribe { handleEvent(it) }
        )
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

    override fun onPause() {
        super.onPause()
        compositeDisposable.clear()
    }
}
```

### Advanced usage

Check the included app sample. If you want to see even more layouts check [Lemuroid](https://github.com/Swordfish90/Lemuroid).

### Random tips
* Do not put margins around RadialGamePad. If you need them use the margin variable, so that touch events will be still forwarded to the view
* If your gamepad is split into multiple views (very likely for games), consider using SecondaryDialConfig.Empty to enforce symmetry.
