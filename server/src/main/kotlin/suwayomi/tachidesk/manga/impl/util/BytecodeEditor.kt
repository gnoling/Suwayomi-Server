package suwayomi.tachidesk.manga.impl.util

/*
 * Copyright (C) Contributors to the Suwayomi project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/. */

import io.github.oshai.kotlinlogging.KotlinLogging
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.streams.asSequence

object BytecodeEditor {
    private val logger = KotlinLogging.logger {}

    /**
     * Replace some java class references inside a jar with new ones that behave like Androids
     *
     * @param jarFile The JarFile to replace class references in
     */
    fun fixAndroidClasses(jarFile: Path) {
        FileSystems.newFileSystem(jarFile, null as ClassLoader?)?.use { fs ->
            val changed =
                Files.walk(fs.getPath("/")).use { paths ->
                    paths
                        .asSequence()
                        .filterNotNull()
                        .filterNot(Files::isDirectory)
                        .mapNotNull(::getClassBytes)
                        .filter { referencesReplacedClass(it.second) }
                        .map(::transform)
                        .onEach(::write)
                        .count()
                }
            if (changed > 0) {
                logger.debug { "Replaced Android class references in $changed classes of $jarFile" }
                // Extensions distributed as signed jars would otherwise fail the class loader's digest check
                removeSignature(fs.getPath("/META-INF"))
            }
        }
    }

    private val signatureFileExtensions = setOf("SF", "RSA", "DSA", "EC")

    private fun removeSignature(metaInf: Path) {
        if (!Files.isDirectory(metaInf)) return
        Files
            .list(metaInf)
            .use { files ->
                files
                    .asSequence()
                    .filter { it.fileName.toString().substringAfterLast('.').uppercase() in signatureFileExtensions }
                    .toList()
            }.forEach(Files::delete)
    }

    /**
     * Cheap pre-check so only classes that need it are rewritten: class names in the
     * constant pool are stored as (ASCII) UTF-8.
     */
    private fun referencesReplacedClass(bytes: ByteArray): Boolean {
        val text = String(bytes, Charsets.ISO_8859_1)
        return classesToReplace.any { text.contains(it) }
    }

    /**
     * Get class bytes from a [Path]
     *
     * @param path The path entry to get the class bytes from
     *
     * @return [Pair] of the [Path] plus the class [ByteArray], or null if it's not a valid class
     */
    private fun getClassBytes(path: Path): Pair<Path, ByteArray>? {
        return try {
            if (path.toString().endsWith(".class")) {
                val bytes = Files.readAllBytes(path)
                if (bytes.size < 4) {
                    // Invalid class size
                    return null
                }
                val cafebabe =
                    String.format(
                        "%02X%02X%02X%02X",
                        bytes[0],
                        bytes[1],
                        bytes[2],
                        bytes[3],
                    )
                if (cafebabe.lowercase() != "cafebabe") {
                    // Corrupted class
                    return null
                }

                path to bytes
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Error loading class from Path: $path" }
            null
        }
    }

    /**
     * The path where replacement classes will reside
     */
    private const val REPLACEMENT_PATH = "xyz/nulldev/androidcompat/replace"

    /**
     * List of classes that will be replaced. Nested classes must be listed explicitly.
     */
    private val classesToReplace =
        listOf(
            "java/text/SimpleDateFormat",
            // android.jar's MotionEvent is a stub; used by extensions that synthesize touches
            "android/view/MotionEvent",
            "android/view/MotionEvent\$PointerCoords",
            "android/view/MotionEvent\$PointerProperties",
            // Virtual display + Presentation, used by extensions to host an "on screen" WebView
            "android/app/Presentation",
            "android/hardware/display/DisplayManager",
            "android/hardware/display/VirtualDisplay",
            "android/media/Image",
            "android/media/ImageReader",
            "android/media/ImageReader\$OnImageAvailableListener",
            "android/view/Display",
            "android/view/Surface",
        )

    private val remapper = SimpleRemapper(classesToReplace.associateWith { "$REPLACEMENT_PATH/$it" })

    /**
     * Replace all references to certain classes inside the class file
     * with ones that behave more like Androids.
     *
     * [ClassRemapper] rewrites every place a class can be referenced (descriptors, signatures,
     * owners of method and field instructions, type instructions, stack map frames,
     * invokedynamic arguments, ...), so the rewritten class stays verifiable.
     *
     * @param pair Class bytecode to load into ASM for ease of modification
     *
     * @return [ByteArray] with modified bytecode
     */
    private fun transform(pair: Pair<Path, ByteArray>): Pair<Path, ByteArray> {
        val cr = ClassReader(pair.second)
        val cw = ClassWriter(0)
        cr.accept(ClassRemapper(cw, remapper), 0)
        return pair.first to cw.toByteArray()
    }

    private fun write(pair: Pair<Path, ByteArray>) {
        Files.write(
            pair.first,
            pair.second,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
        )
    }
}
