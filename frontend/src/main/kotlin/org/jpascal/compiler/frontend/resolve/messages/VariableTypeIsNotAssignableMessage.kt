package org.jpascal.compiler.frontend.resolve.messages

import org.jpascal.compiler.common.Message
import org.jpascal.compiler.common.MessageLevel
import org.jpascal.compiler.frontend.ir.Expression
import org.jpascal.compiler.frontend.ir.SourcePosition
import org.jpascal.compiler.frontend.ir.Variable
import org.jpascal.compiler.frontend.ir.types.Type

data class VariableTypeIsNotAssignableMessage(val variable: Variable, val expression: Expression) : Message {
    override val level: MessageLevel = MessageLevel.ERROR
    override val position: SourcePosition? = variable.position
}