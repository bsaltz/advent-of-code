package com.github.bsaltz.adventofcode.util

import java.io.Reader
import java.io.StringReader

interface Resource {
    fun <T> useLines(block: (Sequence<String>) -> T): T
}

abstract class AbstractReaderResource : Resource {
    override fun <T> useLines(block: (Sequence<String>) -> T): T = createReader().useLines(block)
    protected abstract fun createReader(): Reader
}

data class StringResource(val string: String) : AbstractReaderResource() {
    override fun createReader(): Reader = StringReader(string)
}

data class ClassPathResource(
    val resource: String,
    val classLoader: ClassLoader = ClassPathResource::class.java.classLoader
) : AbstractReaderResource() {
    override fun createReader(): Reader =
        classLoader.getResource(resource)?.openStream()?.bufferedReader()
            ?: throw ClassPathResourceNotFoundException("Resource not found: '$resource'")
}

class ClassPathResourceNotFoundException(message: String) : RuntimeException(message)
fun String.toStringResource(): StringResource = StringResource(this)
fun String.toClassPathResource(): ClassPathResource = ClassPathResource(this)
