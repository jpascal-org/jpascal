package org.jpascal.compiler.backend

import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.ParserFacade
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context
import kotlin.test.assertEquals

abstract class BaseBackendTest {
    protected fun compile(filename: String, code: String, ctx: Context? = null): Class<*> {
        val messageCollector = MessageCollector()
        val context = ctx ?: Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(Source(filename, code), messageCollector)
        context.addExternalLibrary("org.jpascal.stdlib.PreludeKt")
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

    protected fun compile(sources: List<Source>, ctx: Context? = null): Map<String, Class<*>> {
        val messageCollector = MessageCollector()
        val context = ctx ?: Context(messageCollector)
        val parsed = sources.map {
            val parser = createParserFacade()
            parser.parse(it, messageCollector)
        }
        parsed.forEach(context::add)
        parsed.forEach(context::resolve)
        messageCollector.list().forEach {
            println(it)
        }
        assertEquals(0, messageCollector.list().size)
        return parsed.map {
            val generator = ProgramGenerator(it)
            val result = generator.generate()
            writeResult(result)
            result
        }.toMap().toClasses()
    }

    protected fun writeResult(result: CompilationResult) {
        result.write("/tmp/jpascal")
    }

    protected fun CompilationResult.getClass(): Class<*> =
        listOf(this).toMap().toClasses()[this.className.replace('/', '.')]!!

    protected fun createParserFacade(): ParserFacade = AntlrParserFacadeImpl()
}