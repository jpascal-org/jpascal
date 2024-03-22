package org.jpascal.compiler.frontend

import org.jpascal.compiler.frontend.parser.antlr.AntlrParserFacadeImpl
import org.jpascal.compiler.frontend.parser.api.ParserFacade

abstract class BaseFrontendTest {
    protected fun createParserFacade(): ParserFacade = AntlrParserFacadeImpl()

}