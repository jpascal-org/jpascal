package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.ParserFacade
import org.jpascal.compiler.frontend.parser.api.Source
import org.jpascal.compiler.frontend.resolve.Context

abstract class BaseFrontendTest {
    protected fun createParserFacade(): ParserFacade = AntlrParserFacadeImpl()

    protected fun resolve(filename: String, code: String): MessageCollector {
        val messageCollector = MessageCollector()
        val context = Context(messageCollector)
        val parser = createParserFacade()
        val program = parser.parse(Source(filename, code), messageCollector)
        context.add(program)
        context.resolve(program)
        return messageCollector
    }
}