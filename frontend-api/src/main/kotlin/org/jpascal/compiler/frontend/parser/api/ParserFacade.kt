package org.jpascal.compiler.frontend.parser.api

import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.ir.Program

interface ParserFacade {
    fun parse(source: Source, messageCollector: MessageCollector): Program
}