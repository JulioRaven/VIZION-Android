# VIZION for Android

VIZION is a phone/tablet Android app prototype built with Kotlin, Jetpack Compose and AndroidX Media3.

## Current prototype
- Search movie and TV metadata through TMDB (bring your own API key).
- Open a title detail screen.
- Check legal streaming availability by region through TMDB Watch Providers, with a JustWatch link for the selected title.
- Play a user-provided, authorized HTTP(S) MP4, HLS (.m3u8) or DASH stream using Media3.

## Important status
This is an initial buildable foundation, not a finished Netflix-like streaming catalogue. TMDB provides metadata, not video streams. Automatic provider discovery, episode selection, subtitles, watch history and favorites are not implemented yet.

The Gowaru/Nuvio provider bundle is a separate QuickJS plugin ecosystem. Its providers are not directly compatible with a plain Kotlin app. VIZION will only enable sources and streams that the user is authorized to access; it will not bypass DRM, subscriptions, paywalls or extract unauthorized copyrighted streams.

## Build
Requires JDK 17+, Gradle 8.13+ and Android SDK Platform 36.

```bash
gradle assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

## TMDB
Create your own API key at https://www.themoviedb.org/settings/api and enter it in the app. TMDB supplies catalogue metadata and regional watch-provider availability; it does not supply the video streams themselves. Change the two-letter region code in a title page (for example GB, FR or BJ) to check another catalogue.

This product uses the TMDB API but is not endorsed or certified by TMDB. Streaming availability is provided for discovery; playback must happen through the relevant official service unless you provide a stream you are authorized to use.
