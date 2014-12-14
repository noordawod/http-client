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
