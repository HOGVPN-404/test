package com.example.coba.ui.navigation

import android.net.Uri

object Destination {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val HOME = "home"

    const val DOWNLOAD_ARG = "payload"
    const val DOWNLOAD = "download?$DOWNLOAD_ARG={$DOWNLOAD_ARG}"

    const val VIDEO_URL_ARG = "url"
    const val VIDEO_NAME_ARG = "name"
    const val VIDEO_PLAYER = "video?$VIDEO_URL_ARG={$VIDEO_URL_ARG}&$VIDEO_NAME_ARG={$VIDEO_NAME_ARG}"

    fun downloadRoute(payload: String): String {
        return "download?$DOWNLOAD_ARG=${Uri.encode(payload)}"
    }

    fun videoRoute(url: String, filename: String): String {
        return "video?$VIDEO_URL_ARG=${Uri.encode(url)}&$VIDEO_NAME_ARG=${Uri.encode(filename)}"
    }
}
