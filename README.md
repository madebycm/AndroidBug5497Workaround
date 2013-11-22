AndroidBug5497Workaround
========================

WebView adjustResize windowSoftInputMode breaks when activity is fullscreen
___

To use this class, simply invoke ```assistActivity()``` on an Activity that already has its content view set. An example implementation for Cordova/Phonegap is attached.
___
<small>Thanks to [yghm](http://stackoverflow.com/users/2424403/yghm) for the original idea and [Joseph Johnson](http://stackoverflow.com/users/341631/joseph-johnson) for assisting with implementation. For more information, see [issue#5497](https://code.google.com/p/android/issues/detail?id=5497) or [this StackOverflow thread](http://stackoverflow.com/questions/7417123/android-how-to-adjust-layout-in-full-screen-mode-when-softkeyboard-is-visible).</small>
