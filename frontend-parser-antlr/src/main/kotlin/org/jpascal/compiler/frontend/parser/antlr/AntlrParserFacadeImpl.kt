package org.jpascal.compiler.frontend.parser.antlr

import org.antlr.v4.kotlinruntime.CharStream
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.tree.ParseTree
import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.ir.Program
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalLexer
import org.jpascal.compiler.frontend.parser.antlr.generated.JPascalParser
import org.jpascal.compiler.frontend.parser.api.ParserFacade
import org.jpascal.compiler.frontend.parser.api.Source

class AntlrParserFacadeImpl : ParserFacade {
    override fun parse(source: Source, messageCollector: MessageCollector): Program {
        val inputStream: CharStream = CharStreams.fromString(source.code)
        val lex = JPascalLexer(inputStream)
        val stream = CommonTokenStream(lex)
        val parser = JPascalParser(stream)
        parser.addErrorListener(SyntaxErrorListener(source.filename, messageCollector))
        val tree: ParseTree = parser.program()
        val visitor = JPascalVisitorImpl(source.filename)
        visitor.visit(tree)
        return visitor.getProgram() ?: TODO()
    }
}