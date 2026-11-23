package suwayomi.tachidesk.global.impl

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import suwayomi.tachidesk.manga.impl.util.network.await
import suwayomi.tachidesk.server.generated.BuildConfig
import uy.kohesive.injekt.injectLazy

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

data class UpdateDataClass(
    /** [channel] mirrors [suwayomi.tachidesk.server.BuildConfig.BUILD_TYPE] */
    val channel: String,
    val tag: String,
    val url: String,
)

object AppUpdate {
    private const val LATEST_STABLE_CHANNEL_URL = "https://api.github.com/repos/Suwayomi/Suwayomi-Server/releases/latest"
    private const val LATEST_PREVIEW_CHANNEL_URL = "https://api.github.com/repos/Suwayomi/Suwayomi-Server-preview/releases/latest"

    private val json: Json by injectLazy()
    private val network: NetworkHelper by injectLazy()

    suspend fun checkUpdate(): List<UpdateDataClass> {
        val stableJson =
            json
                .parseToJsonElement(
                    network.client
                        .newCall(
                            GET(LATEST_STABLE_CHANNEL_URL),
                        ).await()
                        .body
                        .string(),
                ).jsonObject

        val previewJson =
            json
                .parseToJsonElement(
                    network.client
                        .newCall(
                            GET(LATEST_PREVIEW_CHANNEL_URL),
                        ).await()
                        .body
                        .string(),
                ).jsonObject

        return listOf(
            UpdateDataClass(
                "Stable",
                notOlderThanRunning(stableJson["tag_name"]!!.jsonPrimitive.content),
                stableJson["html_url"]!!.jsonPrimitive.content,
            ),
            UpdateDataClass(
                "Preview",
                notOlderThanRunning(previewJson["tag_name"]!!.jsonPrimitive.content),
                previewJson["html_url"]!!.jsonPrimitive.content,
            ),
        )
    }

    /**
     * Clients offer an update whenever the tag differs from the running version. This build counts the fork's own
     * commits in its revision, so it is always "different" from upstream's releases; report the running version
     * unless the release really is newer.
     */
    private fun notOlderThanRunning(tag: String): String {
        fun parts(version: String) = Regex("""\d+""").findAll(version).map { it.value.toInt() }.toList()
        val release = parts(tag)
        val running = parts(BuildConfig.VERSION)
        for (i in 0 until maxOf(release.size, running.size)) {
            val a = release.getOrElse(i) { 0 }
            val b = running.getOrElse(i) { 0 }
            if (a != b) return if (a > b) tag else BuildConfig.VERSION
        }
        return tag
    }
}
