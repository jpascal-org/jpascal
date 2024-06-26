package org.jpascal.compiler.frontend.controlflow.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.FunctionDeclaration
import org.jpascal.compiler.frontend.ir.SourcePosition

data class MissingReturnStatementMessage(val functionDeclaration: FunctionDeclaration) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = functionDeclaration.position
}
