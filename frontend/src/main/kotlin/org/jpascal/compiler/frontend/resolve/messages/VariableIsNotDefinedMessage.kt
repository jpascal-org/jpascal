package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.Variable

data class VariableIsNotDefinedMessage(val variable: Variable) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = variable.position
}