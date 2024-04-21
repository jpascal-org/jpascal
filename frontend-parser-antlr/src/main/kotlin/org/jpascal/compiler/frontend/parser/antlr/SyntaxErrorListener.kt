package org.jpascal.compiler.frontend.parser.antlr

import org.antlr.v4.kotlinruntime.BaseErrorListener
import org.antlr.v4.kotlinruntime.RecognitionException
import org.antlr.v4.kotlinruntime.Recognizer
import org.jpascal.compiler.frontend.MessageCollector
import org.jpascal.compiler.frontend.ir.Position
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.parser.api.messages.ParseErrorMessage

class SyntaxErrorListener(private val filename: String, private val messageCollector: MessageCollector) :
    BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        val position = Position(line, charPositionInLine + 1)
        messageCollector.add(ParseErrorMessage(msg, SourcePosition(filename, position, position)))
    }
}