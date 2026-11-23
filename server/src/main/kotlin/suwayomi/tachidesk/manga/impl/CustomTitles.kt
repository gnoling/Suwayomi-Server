package suwayomi.tachidesk.manga.impl

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import suwayomi.tachidesk.server.ApplicationDirs
import uy.kohesive.injekt.injectLazy
import java.io.File

/**
 * Display titles the user chose, from [ApplicationDirs.customTitlesFile]: a JSON object mapping
 * manga ids to titles, e.g. `{"271": "One Piece"}`.
 *
 * Sources name a series however they like (romaji, English, ...), so a local copy and an online one
 * of the same series can sort far apart. Only the title clients see changes; the stored title,
 * which names the download directory and is refreshed from the source, stays as it is.
 */
object CustomTitles {
    private val logger = KotlinLogging.logger {}
    private val applicationDirs: ApplicationDirs by injectLazy()
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cache: Pair<Long, Map<Int, String>> = -1L to emptyMap()

    private fun titles(): Map<Int, String> {
        val file = File(applicationDirs.customTitlesFile)
        val modified = if (file.isFile) file.lastModified() else 0L
        val (cachedModified, cachedTitles) = cache
        if (cachedModified == modified) {
            return cachedTitles
        }

        val titles =
            if (modified == 0L) {
                emptyMap()
            } else {
                runCatching { json.decodeFromString<Map<String, String>>(file.readText()) }
                    .onFailure { logger.warn(it) { "Ignoring unreadable custom titles file ${file.path}" } }
                    .getOrDefault(emptyMap())
                    .mapNotNull { (id, title) -> id.toIntOrNull()?.let { it to title.trim() } }
                    .filter { (_, title) -> title.isNotEmpty() }
                    .toMap()
            }
        cache = modified to titles
        return titles
    }

    fun displayTitle(
        mangaId: Int,
        title: String,
    ): String = titles()[mangaId] ?: title
}
