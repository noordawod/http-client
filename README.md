HTTP client
===========

An asynchronous HTTP client and download manager for Android.

This is based on the excellent
[AsyncHttpClient](https://github.com/loopj/android-async-http) (AHC for short)
originally developed by James Smith and continues to be maintained by many
developers (including myself).

Although AHC provides the infrastructure to work with HTTP, it comes with almost
no higher-level mechanisms, like a download manager, caching and one single
callback handler.

This library tries to address these shortcomings and builds on top of AHC to
provide additional functionality. If you are already using AHC to drive your
app, switching to this library is extremely easy.

Dependencies
------------

The library depends on these libraries:

* [AsyncHttpClient](https://github.com/loopj/android-async-http)
* [Android-Cache](https://github.com/noordawod/android-cache) -- simple, fast and
  robust caching library for Android (also written by myself)
* [JsonInterface](https://github.com/noordawod/json-interface) -- an interface 
  for working with any JSON implementation (also written by myself)

At the moment, I have not pushed any Maven or Gradle artifcats so you'll have to
manage with picking up the different JARs of these libraries and placing them
in the libs/ folder of your app. It's on the TODO list, though :)

License
-------
This library is open source and released under the terms of MIT license.
You will find a LICENSE file in the library's root directory.
