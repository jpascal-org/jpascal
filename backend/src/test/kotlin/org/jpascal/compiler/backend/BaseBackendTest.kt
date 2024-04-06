package org.jpascal.compiler.backend

import org.jpascal.compiler.backend.utils.ByteArrayClassLoader
import org.jpascal.compiler.common.MessageCollector
import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.ParserFacade
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import java.io.File
import kotlin.test.assertEquals

abstract class BaseBackendTest {
    protected fun program(filename: String, code: String, ctx: Context? = null): Class<*> {
        val messageCollector = MessageCollector()
        val context = ctx ?: Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(Source(filename, code))
        context.add(program)
        context.resolve(program)
        messageCollector.list().forEach {
            println(it)
        }
        assertEquals(0, messageCollector.list().size)
        val generator = ProgramGenerator(program)
        val result = generator.generate()
        writeResult(result)
        return result.getClass()
    }

    protected fun writeResult(result: CompilationResult) {
        File("/tmp/jpascal/${result.className}.class").writeBytes(result.bytecode)
    }

    private fun Map<String, ByteArray>.toClasses(): Map<String, Class<*>> {
        val loader = ByteArrayClassLoader(this)
        return mapValues { loader.loadClass(it.key) }
    }

    private fun List<CompilationResult>.toMap() = this.associate { it.className to it.bytecode }

    protected fun CompilationResult.getClass(): Class<*> = listOf(this).toMap().toClasses()[this.className]!!

    protected fun createParserFacade(): ParserFacade = AntlrParserFacadeImpl()
}