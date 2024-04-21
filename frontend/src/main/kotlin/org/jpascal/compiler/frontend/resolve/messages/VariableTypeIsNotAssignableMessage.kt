package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.frontend.Message
import org.jpascal.compiler.frontend.MessageLevel
import org.jpascal.compiler.frontend.ir.Expression
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.Variable

data class VariableTypeIsNotAssignableMessage(val variable: Variable, val expression: Expression) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = variable.position
}