package org.jpascal.compiler.frontend.parser.api.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.SourcePosition

data class ParseErrorMessage(val message: String, override val position: SourcePosition?) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
}
