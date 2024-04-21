package org.jpascal.compiler.backend

import java.io.File

fun Map<String, ByteArray>.toClasses(): Map<String, Class<*>> {
    val loader = ByteArrayClassLoader(this)
    return mapValues { loader.loadClass(it.key) }
}

fun List<CompilationResult>.toMap() = this.associate { it.className.replace('/', '.') to it.bytecode }

fun CompilationResult.write(outputDirectory: String?) {
    val file = File(
        (outputDirectory?.let { it + File.separator } ?: "") +
                className.replace('/', File.separatorChar) + ".class"
    )
    if (file.parentFile != null && !file.parentFile.exists()) file.parentFile.mkdirs()
    file.writeBytes(bytecode)
}