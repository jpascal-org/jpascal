package org.jpascal.compiler.backend

import java.net.URL
import java.net.URLClassLoader

class ByteArrayClassLoader(extraClassDefs: Map<String, ByteArray>, urls: Array<URL> = arrayOf()) :
    URLClassLoader(urls) {
    private val extraClassDefs: MutableMap<String, ByteArray> = extraClassDefs.toMutableMap()

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        val classBytes = extraClassDefs.remove(name)
        return if (classBytes != null) {
            defineClass(name, classBytes, 0, classBytes.size)
        } else super.findClass(name)
    }
}